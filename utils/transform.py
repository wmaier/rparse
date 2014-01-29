#!/usr/bin/python
""" Tools for transforming treebank trees.

Authors: Wolfgang Maier <maierw@hhu.de>
Version: 29 January 2014
"""
from __future__ import print_function, with_statement, division
import getopt
import sys
import io
import copy

USAGE = """Usage: %s [OPTIONS] ALGORITHM INPUT OUTPUT 
Transform trees in INPUT using ALGORITHM and write result to OUTPUT.

  -e, --inputencoding=ENC      encoding of INPUT (default latin-1)
  -i, --inputformat=FORMAT     format of INPUT (default export)
  -o, --outputformat=FORMAT    format of OUTPUT (default export3)
  -s, --split=HOW              split output into n files OUTPUT.(0..n-1).
                               HOW is a string specifying the distribution 
                               of sentences into parts. Each of the n parts 
                               is specified by a number followed either by 
                               '#' (denoting a certain number of sentences) 
                               or '%%' (denoting a percentage). One part can
                               use the keyword 'rest'. Part specifications
                               must be separated by a single '_'.
  -h, --help                   display this help and exit

Available algorithms (see docstrings)
  none                         Unmodified output (modulo modifications made by
                               input readers).
  root                         Reattach all children of VROOT to the tree
  split                        Node splitting as in Boyd (1997).
  split_marking                As above. Additionally marks newly introduced
                               nodes.
  raising                      Remove crossing branches by re-attaching those
                               nodes to higher positions which introduce cro-
                               ssing branches. 

Available input formats
  export                       Export format (3 or 4). Ignores all fields
                               after the parent number since not all export
                               treebanks respect the original export definition
                               from Brants (1997) (see TueBa-D/Z 8). Tree ids
                               are assigned by counting (not by BOS/EOS).

Available output formats: 
  export3                      Export format 3 (missing lemma will be 
                               substituted with --)
  export4                      Export format 4 (with lemma)

A single (sub)tree is represented by a dict with obligatory keys
DATA, CHILDREN, PARENT. We do not rely on the children to be ordered. 
The function children() can be used to get an ordered list of children
ordered by the respective leftmost terminal they dominate. If there 
are no children, there must be a NUM key which denotes the position 
index. Repeated or unspecified indices are an error. Missing CHILDREN
or PARENT keys are also an error too (use make_tree()).

Tree readers are implemented as generators reading from a file with
a given encoding and yielding trees. Tree writer functions write 
single trees plus id to a given stream, always as UTF8. Tree transformation
functions take a tree as input and return the transformed tree. 
""" % sys.argv[0]
NUMBER_OF_FIELDS = 6
WORD, LEMMA, LABEL, MORPH, \
    EDGE, PARENT_NUM = tuple(range(NUMBER_OF_FIELDS))
FIELDS = [WORD, LEMMA, LABEL, MORPH, EDGE, PARENT_NUM]
NUM = 'num'
DATA = 'data'
CHILDREN = 'children'
PARENT = 'parent'

#
# Tree modification
#

def nothing(tree):
    """Do nothing."""
    return tree


def root_attach(tree):
    """Reattach some children of the virtual root node in NeGra/TIGER/TueBa-DZ. 
    In a nutshell, the algorithm moves all children of VROOT to the least 
    common ancestor of the left neighbor terminal of the leftmost terminal and 
    the right neighbor terminal of the rightmost terminal they dominate. We 
    iterate through the children of VROOT left to right. Therefore, we might 
    have to skip over adjacent children of VROOT on the right (which are not 
    attached yet) in order to find the rightmost terminal. If the VROOT child 
    constitutes the start or end of the sentence, or if the least common 
    ancestor as described above is VROOT, it is not moved.""" 
    tree_terms = terminals(tree)
    # numbers of leftmost and rightmost terminal
    tree_min = tree_terms[0][NUM]
    tree_max = tree_terms[-1][NUM]
    # iterate through all VROOT children and try to attach them to the tree,
    # proceed left to right
    for child in children(tree):
        # indices of terminal children of current child
        term_ind = [terminal[NUM] for terminal in terminals(child)]
        # left and right neighbor of lefmost and rightmost terminal child
        t_l = min(term_ind) - 1
        t_r = max(term_ind) + 1
        # on the right, we have to skip over all adjacent terminals which are 
        # dominated by siblings of the current child of VROOT
        focus = child
        sibling = right_sibling(focus)
        while not sibling == None:
            focus_ind = [terminal[NUM] for terminal in terminals(focus)]
            sibling_ind = [terminal[NUM] for terminal in terminals(sibling)]
            # skip over sibling if it starts left of the end
            # of the current focus node. Example: right sibling of current
            # child is a phrase, sibling of the phrase is punctuation 
            # which interrupts this same phrase
            if min(sibling_ind) < max(focus_ind):
                sibling = right_sibling(sibling)
                continue
            # gap found, i.e., sibling not adjacent to current node: we are done
            if min(sibling_ind) > max(focus_ind) + 1:
                break
            # neither skip nor done: update right boundary and try next sibling 
            t_r = max(sibling_ind) + 1
            focus = sibling
            sibling = right_sibling(sibling)
        # ignore if beyond sentence
        if t_l < tree_min or t_r > tree_max:
            continue
        # target for movement is least common ancestor of terminal neighbors
        target = lca(tree_terms[t_l - 1], tree_terms[t_r - 1])
        # move/attach node
        child[PARENT][CHILDREN].remove(child)
        target[CHILDREN].append(child)
        child[PARENT] = target
    return tree


def mark_heads(tree):
    """Mark the head child of each node in a NeGra/TIGER tree using a simple 
    heuristic. If there is child with a HD edge, it will be marked. Otherwise,
    the rightmost child with a NK edge will be marked. If there is no such
    child, the leftmost child will be marked."""
    tree['HEAD'] = False
    for subtree in preorder(tree):
        if has_children(subtree):
            subtree_children = children(subtree)
            edges = [child[DATA][EDGE] for child in subtree_children]
            # default leftmost
            index = 0
            # if applicable leftmost HD
            if 'HD' in edges:
                index = edges.index('HD')
            # otherwise if applicable rightmost NK
            elif 'NK' in edges:
                index = (len(edges) - 1) - edges[::-1].index('NK') 
            subtree_children[index]['HEAD'] = True
            for i, child in enumerate(subtree_children):
                if not i == index:
                    child['HEAD'] = False
    return tree


def boyd_split(tree, **params):
    """For each continuous terminal block of a discontinuous node in tree, 
    introduce a node which covers exactly this block. A single unique
    node is marked as head block if it covers the original head daugther
    of the unsplit node, to be determined recursively in case the head
    daugther has been split itself. For head finding a simple heuristic 
    is used. The algorithm is documented in Boyd (2007) (ACL-LAW workshop).
    The algorithm relies on a previous application of head marking."""
    # postorder since we have to 'continuify' lower trees first
    for subtree in postorder(tree):
        # set default values
        subtree['SPLIT'] = False
        subtree['HEAD_BLOCK'] = True
        # split the children such that each sequence of children dominates
        # a continuous block of terminals
        blocks = []
        for child in children(subtree):
            if len(blocks) == 0:
                blocks.append([])
            else:
                last_terminal = terminals(blocks[-1][-1])[-1][NUM]
                if terminals(child)[0][NUM] > last_terminal + 1:
                    blocks.append([])
            blocks[-1].append(child)
        parent = subtree[PARENT]
        # more than one block: do splitting.
        split = []
        if len(blocks) > 1:
            # unhook node
            parent[CHILDREN].remove(subtree)
            subtree[PARENT] = None
            # for each of the blocks, create a split node
            for i, block in enumerate(blocks):
                # the new node:
                split.append(make_tree(subtree[DATA]))
                split[-1]['SPLIT'] = True
                split[-1]['HEAD'] = subtree['HEAD']
                split[-1]['HEAD_BLOCK'] = False
                if params['marking']:
                    split[-1][DATA][LABEL] += u"*%d" % (i + 1)
                parent[CHILDREN].append(split[-1])
                split[-1][PARENT] = parent
                # iterate through children of original node in
                # the current block
                for child in block:
                    # mark current block as head block if the current child has
                    # the head attribute set (if the current child is a split 
                    # node, it must also be marked as covering head block)
                    split[-1]['HEAD_BLOCK'] = split[-1]['HEAD_BLOCK'] \
                        or child['HEAD'] and \
                        ((not child['SPLIT']) or child['HEAD_BLOCK'])
                    # move child below new block node
                    subtree[CHILDREN].remove(child)
                    split[-1][CHILDREN].append(child)
                    child[PARENT] = split[-1]
    return tree


def raising(tree):
    """Remove crossing branches by 'raising' nodes which cause crossing
    branches. This algorithm relies on a previous application of the Boyd
    splitting and removes all those newly introduced nodes which are *not*
    marked as head block (see above)."""
    removal = []
    for subtree in preorder(tree):
        if not subtree == tree:
            if subtree['SPLIT']:
                if not subtree['HEAD_BLOCK']:
                    removal.append(subtree)
    for subtree in removal:
        parent = subtree[PARENT]
        parent[CHILDREN].remove(subtree)
        subtree[PARENT] = None
        for child in children(subtree):
            subtree[CHILDREN].remove(child)
            parent[CHILDREN].append(child)
            child[PARENT] = parent
    return tree

#
# Operations on trees
#

def make_tree(data):
    """Make an empty tree and possibly copy data from another tree."""
    return { CHILDREN : [], PARENT : None, DATA : copy.deepcopy(data) }


def preorder(tree):
    """Generator which performs a preorder tree traversal and yields
    the subtrees encountered on its way."""
    yield tree
    for child in children(tree):
        for child_tree in preorder(child):
            yield child_tree


def postorder(tree):
    """Generator which performs a postorder tree traversal and yields
    the subtrees encountered on its way."""
    for child in children(tree):
        for child_tree in postorder(child):
            yield child_tree
    yield tree


def children(tree):
    """Return the ordered children of the root of this tree."""
    return sorted(tree[CHILDREN], key=lambda x: terminals(x)[0][NUM])


def has_children(tree):
    """Return true if this tree has any child."""
    return len(tree[CHILDREN]) > 0


def terminals(tree):
    """Return all terminal children of this subtree."""
    if len(tree[CHILDREN]) == 0:
        return [tree]
    else:
        result = []
        for child in tree[CHILDREN]:
            result.extend(terminals(child))
        return sorted(result, key=lambda x: x[NUM])


def right_sibling(tree):
    """Return the right sibling of this tree if it exists and None otherwise."""
    siblings = children(tree[PARENT])
    for (index, _) in enumerate(siblings[:-1]):
        if siblings[index] == tree:
            return siblings[index + 1]
    return None


def lca(tree_a, tree_b):
    """Return the least common ancestor of two trees and None if there 
    is none."""
    dom_a = [tree_a]
    parent = tree_a
    while not parent[PARENT] == None:
        parent = parent[PARENT]
        dom_a.append(parent)
    dom_b = [tree_b]
    parent = tree_b
    while not parent[PARENT] == None:
        parent = parent[PARENT]
        dom_b.append(parent)
    i = 0
    for i, (el_a, el_b) in enumerate(zip(dom_a[::-1], dom_b[::-1])):
        if not el_a == el_b:
            return dom_a[::-1][i - 1]
    return None


#
# Writing trees
#

def export_tabs(length):
    """Number of tabs after a single field in export format, given the
    length of the field."""
    if length < 8:
        return "\t\t\t"
    elif length < 16:
        return "\t\t"
    else:
        return "\t"


def export_format(subtree, is_four):
    """Return an export formatted node line for a given subtree."""
    data = subtree[DATA]
    if data[EDGE] == None:
        data[EDGE] = '--'
    if not is_four:
        return u"%s%s%s\t%s%s%s\t%d\n" \
            % (data[WORD], export_tabs(len(data[WORD])), \
               data[LABEL], data[MORPH], export_tabs(len(data[MORPH]) + 8), \
               data[EDGE], subtree[PARENT][NUM])
    else:
        return u"%s%s%s%s%s\t%s%s%s\t%d\n" \
            % (data[WORD], export_tabs(len(data[WORD])), \
               data[LEMMA], export_tabs(len(data[LEMMA])), \
               data[LABEL], data[MORPH], export_tabs(len(data[MORPH]) + 8), \
               data[EDGE], subtree[PARENT][NUM])


def compute_export_numbering(tree):
    """Compute the export node numbering for a tree. The idea is as follows:
    We compute the 'level' of each node, i.e., its minimal path length to a
    terminal. We then distribute numbers >= 500 from left to right in each
    level, starting with the lowest one."""
    levels = {}
    for subtree in preorder(tree):
        if has_children(subtree):
            level = 0
            for terminal in terminals(subtree):
                path_length = 0
                path_element = terminal
                while not path_element == subtree:
                    path_element = path_element[PARENT]
                    path_length += 1
                level = max(level, path_length)
            if not level in levels:
                levels[level] = []
            levels[level].append(subtree)
    for level in levels:
        levels[level] = sorted(levels[level], \
                                   key=lambda x: terminals(x)[0][NUM])
    num = 500
    for level_num in sorted(levels.keys()):
        level = levels[level_num]
        for subtree in level:
            subtree[NUM] = num
            num += 1
    tree[NUM] = 0


def write_export(tree, **params): 
    """Write a tree in export format to a stream."""
    tree_id = params['tree_id']
    stream = params['stream']
    is_four = params['is_four']
    compute_export_numbering(tree)
    stream.write(u"#BOS %d\n" % tree_id)
    terms = {}
    non_terms = {}
    for subtree in preorder(tree):
        if subtree == tree:
            continue
        subtree[DATA][PARENT_NUM] = u"%d" % subtree[PARENT][NUM]
        if has_children(subtree):
            subtree[DATA][WORD] = u"#%d" % subtree[NUM]
            non_terms[subtree[NUM]] = export_format(subtree, is_four)
        else:
            terms[subtree[NUM]] = export_format(subtree, is_four)
    for num in sorted(terms.keys()):
        stream.write(terms[num])
    for num in sorted(non_terms.keys()):
        stream.write(non_terms[num])
    stream.write(u"#EOS %d\n" % tree_id)


#
# Reading trees
#

def export_build_tree(num, node_by_num, children_by_num):
    """ Build a tree from export. """
    tree = make_tree(node_by_num[num])
    tree['terminals'] = []
    if num in children_by_num:
        tree[CHILDREN] = []
        for child in children_by_num[num]:
            child_tree = export_build_tree(child, node_by_num, children_by_num)
            child_tree[PARENT] = tree
            tree[CHILDREN].append(child_tree)
            tree['terminals'].extend(child_tree['terminals'])
        tree[CHILDREN] = sorted(tree[CHILDREN], \
                                    key=lambda x: min(x['terminals']))
    else:
        tree[CHILDREN] = []
        tree['terminals'] = [num]
        tree[NUM] = num
    tree['terminals'] = sorted(tree['terminals'])
    return tree


def export_parse_line(line):
    """ Parse a single export format line, i.e., one node."""
    fields = line.split()
    # if it is export 3, insert dummy lemma
    if fields[4].isdigit():
        fields[1:1] = [u"--"]
    if len(fields) < NUMBER_OF_FIELDS:
        raise ValueError("too few fields")
    # throw away after parent number and assign to fields
    fields = dict(zip(FIELDS, fields[:NUMBER_OF_FIELDS])) 
    fields[PARENT_NUM] = int(fields[PARENT_NUM])
    if not (500 <= fields[PARENT_NUM] < 1000 or fields[PARENT_NUM] == 0):
        raise ValueError("parent field must be 0 or between 500 and 999")
    return fields


def parse_export(in_file, in_encoding):
    """Generator which parses an export file. Collect node data and child 
    references and yield trees as described above. """
    in_sentence = False
    sentence = []
    with io.open(in_file, encoding=in_encoding) as stream:
        for line in stream:
            line = line.strip()
            if not in_sentence:
                if line.startswith(u"#BOS"):
                    in_sentence = True
                    sentence.append(line)
            else:
                sentence.append(line)
                if line.startswith(u"#EOS"):
                    node_by_num = {}
                    children_by_num = {}
                    node_by_num[0] = [None] * NUMBER_OF_FIELDS
                    node_by_num[0][LABEL] = u"VROOT"
                    term_cnt = 1
                    for fields in [export_parse_line(line) \
                                       for line in sentence[1:-1]]:
                        word = fields[WORD]
                        num = None
                        if len(word) == 4 and word[0] == u"#" \
                                and word[1:].isdigit():
                            num = int(word[1:])
                        else:
                            num = term_cnt
                            term_cnt += 1
                        if not 0 <= num <= 999:
                            raise ValueError("node number must 0 and 999")
                        node_by_num[num] = fields
                        if not fields[PARENT_NUM] in children_by_num:
                            children_by_num[fields[PARENT_NUM]] = []
                        children_by_num[fields[PARENT_NUM]].append(num)
                    yield export_build_tree(0, node_by_num, children_by_num)
                    in_sentence = False
                    sentence = []


def parse_split_specification(split_spec, size):
    """Parse the specification of part sizes for output splitting.
    The specification must be given as list of part size specifications 
    separated by underscores where each part size specification is either a
    number suffixed by '#' (denoting an absolute size) or '%%' (denoting
    a percentage), or the keyword 'rest' which may occur once (denoting
    the part which receives the difference between the given number of
    trees and the sum of trees distributed into other parts given by the
    numerical part size specifications). """
    parts = []
    rest_index = None # remember where the 'rest' part is
    for i, part_spec in enumerate(split_spec.split('_')):
        if part_spec[-1] == "%":
            parts.append(int(round((int(part_spec[:-1]) / 100) * size)))
        elif part_spec[-1] == "#":
            parts.append(int(part_spec[:-1]))
        elif part_spec == 'rest':
            if rest_index == None:
                parts.append(0)
                rest_index = i
            else:
                raise ValueError("'rest' keyword used more than once")
        else:
            raise ValueError("cannot parse specification '%s'" % split_spec)
    # check if it makes sense
    sum_parts = sum(parts)
    if sum_parts < size:
        diff = size - sum_parts
        if not rest_index == None:
            parts[rest_index] = diff
        else:
            sys.stderr.write("rounding: extra %d sentences will be added\n")
            sys.stderr.write("to part with the largest number of\n" % diff)
            sys.stderr.write("sentences. In case of a tie, the sentences\n")
            sys.stderr.write("are added to the first part.\n")
            parts[parts.index(max(parts))] += diff
    elif sum_parts == size:
        if not rest_index == None:
            sys.stderr.write("warning: 'rest' part will be empty\n")
    elif sum_parts > size:
        raise ValueError("treebank smaller than sum of split (%d vs %d)\n" \
                             % (size, sum_parts))
    return parts

# register available stuff 
# reading trees
PARSER = { 'export' : parse_export }
# writing trees
OUTPUT = { 'export3' : (write_export, {'is_four' : False}), \
               'export4' : (write_export, {'is_four' : True})
           }
# transformation of trees
ALGORITHMS = { 'a_none' : (nothing, {}), \
                   'a_root' : (root_attach, {}), \
                   'a_mark_heads' : (mark_heads, {}), \
                   'a_boyd' : (boyd_split, { 'marking' : False }), \
                   'a_boyd_marking' : (boyd_split, { 'marking' : True }), \
                   'a_raising' : (raising, {})
               }
# piplines of transformation algorithms
PIPELINE = { 'none' : ['a_none'], \
                 'root' : ['a_root'], \
                 'mark_heads' : ['a_mark_heads'], \
                 'split' : ['a_root', 'a_mark_heads', 'a_boyd'], \
                 'split_marking' : ['a_root', 'a_mark_heads', \
                                        'a_boyd_marking'], \
                 'raising' : ['a_root', 'a_mark_heads', 'a_boyd', \
                                  'a_raising']
             }

def main():
    """Parse command line and run uncrossing."""
    try:
        (opts, args) = getopt.getopt(sys.argv[1:], 'hi:e:o:s:', [
            'help',
            'inputformat=', 
            'inputencoding=',
            'outputformat=',
            'split='
            ])
    except getopt.GetoptError, err:
        sys.stderr.write("%s: %s\nTry '%s --help' for more information.\n" \
                             % (sys.argv[0], str(err), sys.argv[0]))
        sys.exit(1)
    # defaults
    in_encoding = 'latin-1'
    in_format = 'export'
    out_format = 'export3'
    split_spec = None
    for (opt, arg) in opts:
        if opt in ('-h', '--help'):
            print(USAGE)
            sys.exit(0)
        elif opt in ('-i', '--inputformat'):
            in_format = arg
        elif opt in ('-e', '--inputencoding'):
            in_encoding = arg
        elif opt in ('-o', '--outputformat'):
            out_format = arg
        elif opt in ('-s', '--split'):
            split_spec = arg
    if len(args) < 3:
        sys.stderr.write("%s: %s\nTry '%s --help' for more information.\n" \
                             % (sys.argv[0], "Missing arguments.", sys.argv[0]))
        sys.exit(1)
    pipeline, in_file, out_file = sys.argv[-3:]
    sys.stderr.write("reading from '%s' in encoding '%s'\n" \
                     % (in_file, in_encoding))
    sys.stderr.write("applying '%s'\n" % PIPELINE[pipeline])
    sys.stderr.write("writing to '%s' in format '%s' and encoding 'utf-8'\n" \
                         % (out_file, out_format))
    if not split_spec == None:
        sys.stderr.write("splitting output like this: %s\n" % split_spec)
    cnt = 1
    if split_spec == None:
        # read, process and write trees
        with io.open(out_file, 'w', encoding='utf-8') as out_stream:
            # set output stream for output function
            OUTPUT[out_format][1].update({'stream' : out_stream})
            for tree in PARSER[in_format](in_file, in_encoding):
                for algorithm in PIPELINE[pipeline]:
                    tree = ALGORITHMS[algorithm][0](tree, \
                                                        **ALGORITHMS[algorithm][1])
                # set sentence number 
                OUTPUT[out_format][1].update({'tree_id' : cnt})
                OUTPUT[out_format][0](tree, **OUTPUT[out_format][1])
                if cnt % 100 == 0:
                    sys.stderr.write("\r%d" % cnt)
                cnt += 1
    else:
        # read and process trees
        cnt = 1
        trees = []
        for tree in PARSER[in_format](in_file, in_encoding):
            for algorithm in PIPELINE[pipeline]:
                tree = ALGORITHMS[algorithm][0](tree, \
                                                    **ALGORITHMS[algorithm][1])
                trees.append(tree)
                if cnt % 100 == 0: sys.stderr.write("\r%d" % cnt)
                cnt += 1
        sys.stderr.write("\n")
        parts = parse_split_specification(split_spec, len(trees))
        # write the parts
        sys.stderr.write("writing parts of sizes %s\n" % str(parts))
        for i, part_size in enumerate(parts):
            sys.stderr.write("writing part %d\n" % i)
            with io.open("%s.%d" % (out_file, i), 'w', encoding='utf-8') \
                    as out_stream:
                OUTPUT[out_format][1].update({'stream' : out_stream})
                for tree_id in range(0, part_size):
                    OUTPUT[out_format][1].update({'tree_id' : tree_id + 1})
                    OUTPUT[out_format][0](tree, **OUTPUT[out_format][1])
                    if tree_id % 100 == 0: sys.stderr.write("\r%d" % tree_id)
                    tree_id += 1
                sys.stderr.write("\n")
        sys.stderr.write("done\n")

if __name__ == '__main__':
    main()

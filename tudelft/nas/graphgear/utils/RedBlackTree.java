/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * source: en.literateprograms.org/Red-black_tree_(Java)
 * see site for comments and explenation
 */

package tudelft.nas.graphgear.utils;

public class RedBlackTree{

    /**
     * Creates new tree. source code: en.literateprograms.org/Red-black_tree_(Java)
     */
    public RedBlackTree(){
        root = null;
    }

    public long size() {
        return size;
    }
    private void replaceNode(RBNode oldn, RBNode newn) {
        if(oldn.parent == null){
            root = newn;
        }
        else
        {
            if(oldn == oldn.parent.left)
            {
                oldn.parent.left = newn;
            }
            else
            {
                oldn.parent.right = newn;
            }
        }
        if(newn != null)
        {
            newn.parent = oldn.parent;
        }

    }
    private void rotateLeft(RBNode n) {
        RBNode r = n.right;
        replaceNode(n,r);
        n.right = r.left;
        if(r.left != null){
            r.left.parent = n;
        }
        r.left = n;
        n.parent = r;
    }
    private void rotateRight(RBNode n) {
        RBNode l = n.left;
        replaceNode(n,l);
        n.left = l.right;
        if(l.right != null){
            l.right.parent = n;
        }
        l.right = n;
        n.parent = l;
    }
    private static NodeColor nodeColor(RBNode n) {
        return n == null ? NodeColor.BLACK : n.color;
    }
    private RBNode createNode(Comparable content){
        RBNode result;
        if(stash != null)
        {
            result = stash;
            stash = stash.right;
            result.right = null;
            result.content = content;
        }
        else
        {
            result = new RBNode(content);
            created++;
        }
        return result;
    }
    public void returnNode(RBNode in){
        // strip node of its previous life
        in.content = null;
        in.left = null;
        in.parent = null;
        in.right = null;
        in.color = null;
        if(stash == null)
        {
            stash = in;
        }
        else
        {
            in.right = stash;
            stash = in;
        }
    }
    public void insert(Comparable in){
        if(smallest == null || in.compareTo(smallest) < 0)
        {
            smallest = in;
        }
        // Create new node with in as content
        RBNode insertedNode = createNode(in);

        if (root == null)
        {
            insertedNode.color = NodeColor.RED;
            insertedNode.left = null;
            insertedNode.right = null;
            insertedNode.parent = null;
            root = insertedNode;
        }
        else
        {
            RBNode n = root;
            while (true)
            {
                compResult = insertedNode.compareTo(n);
                if (compResult == 0)
                {
                    System.out.println("Duplicate Node error");
                    System.out.println(n + " and " + insertedNode);
                    return;
                }
                else if (compResult < 0)
                {
                    if (n.left == null)
                    {
                        insertedNode.color = NodeColor.RED;
                        insertedNode.left = null;
                        insertedNode.right = null;
                        insertedNode.parent = null;
                        n.left = insertedNode;
                        break;
                    }
                    else
                    {
                        n = n.left;
                    }
                }
                else
                {
                    assert compResult > 0;
                    if (n.right == null)
                    {
                        insertedNode.color = NodeColor.RED;
                        insertedNode.left = null;
                        insertedNode.right = null;
                        insertedNode.parent = null;
                        n.right = insertedNode;
                        break;
                    }
                    else
                    {
                        n = n.right;
                    }
                }
            }
            insertedNode.parent = n;
        }
        insertCase1(insertedNode);
        size++;
    }
    private void insertCase1(RBNode n) {
        if (n.parent == null)
        {
            n.color = NodeColor.BLACK;
        }
        else
        {
            insertCase2(n);
        }
    }
    private void insertCase2(RBNode n) {
        if (nodeColor(n.parent) == NodeColor.BLACK)
        {
            return; // Tree is still valid
        }
        else
        {
            insertCase3(n);
        }
    }
    void insertCase3(RBNode n) {
        if (nodeColor(n.uncle()) == NodeColor.RED)
        {
            n.parent.color = NodeColor.BLACK;
            n.uncle().color = NodeColor.BLACK;
            n.grandparent().color = NodeColor.RED;
            insertCase1(n.grandparent());
        }
        else
        {
            insertCase4(n);
        }
    }
    void insertCase4(RBNode n) {
        if (n == n.parent.right && n.parent == n.grandparent().left)
        {
            rotateLeft(n.parent);
            n = n.left;
        }
        else if (n == n.parent.left && n.parent == n.grandparent().right)
        {
            rotateRight(n.parent);
            n = n.right;
        }
        insertCase5(n);
    }
    void insertCase5(RBNode n) {
        n.parent.color = NodeColor.BLACK;
        n.grandparent().color = NodeColor.RED;
        if (n == n.parent.left && n.parent == n.grandparent().left)
        {
            rotateRight(n.grandparent());
        } else
        {
            assert n == n.parent.right && n.parent == n.grandparent().right;
            rotateLeft(n.grandparent());
        }
    }
    public Comparable removeMinimum(){
        RBNode min = minimumNode(root);
        Comparable res = min.content;
        delete(min);
        returnNode(min);
        return res;
    }
    private  RBNode minimumNode(RBNode n){
        assert n != null;
        while (n.left != null) {
            n = n.left;
        }
        if(n.right == null)
        {
            if(n.parent != null)
            {
                smallest = n.parent.content;
            }
        }
        else
        {
            if(n.right != null)
            {
                smallest = n.right.content;
            }
        }
        return n;
    }
    private static RBNode maximumNode(RBNode n) {
        assert n != null;
        while (n.right != null) {
            n = n.right;
        }
        return n;
    }
    public void remove(Comparable in){
        delete(lookupNode(in));
    }
    private RBNode lookupNode(Comparable in) {
        RBNode n = root;
        while (n != null) {
            compResult = in.compareTo(n.content);
            if (compResult == 0) {
                return n;
            } else if (compResult < 0) {
                n = n.left;
            } else {
                assert compResult > 0;
                n = n.right;
            }
        }
        return n;
    }
    private RBNode delete(RBNode n) {
        if (n == null)
            return null;  // Key not found, do nothing
        if (n.left != null && n.right != null) {
            // Copy key/value from predecessor and then delete it instead
            RBNode pred = maximumNode(n.left);
            n.CopyContent(pred);
            n = pred;
        }
        assert n.left == null || n.right == null;
        RBNode child = (n.right == null) ? n.left : n.right;
        if (nodeColor(n) == NodeColor.BLACK)
        {
            n.color = nodeColor(child);
            deleteCase1(n);
        }
        replaceNode(n, child);
        if (nodeColor(root) == NodeColor.RED)
        {
            root.color = NodeColor.BLACK;
        }
        size--;
        return n;
    }
    private void deleteCase1(RBNode n) {
        if (n.parent == null)
            return;
        else
            deleteCase2(n);
    }
    private void deleteCase2(RBNode n) {
        if (nodeColor(n.sibling()) == NodeColor.RED) {
            n.parent.color = NodeColor.RED;
            n.sibling().color = NodeColor.BLACK;
            if (n == n.parent.left)
                rotateLeft(n.parent);
            else
                rotateRight(n.parent);
        }
        deleteCase3(n);
    }
    private void deleteCase3(RBNode n) {
        if (nodeColor(n.parent) == NodeColor.BLACK &&
            nodeColor(n.sibling()) == NodeColor.BLACK &&
            nodeColor(n.sibling().left) == NodeColor.BLACK &&
            nodeColor(n.sibling().right) == NodeColor.BLACK)
        {
            n.sibling().color = NodeColor.RED;
            deleteCase1(n.parent);
        }
        else
            deleteCase4(n);
    }
    private void deleteCase4(RBNode n) {
        if (nodeColor(n.parent) == NodeColor.RED &&
            nodeColor(n.sibling()) == NodeColor.BLACK &&
            nodeColor(n.sibling().left) == NodeColor.BLACK &&
            nodeColor(n.sibling().right) == NodeColor.BLACK)
        {
            n.sibling().color = NodeColor.RED;
            n.parent.color = NodeColor.BLACK;
        }
        else
            deleteCase5(n);
    }
    private void deleteCase5(RBNode n) {
        if (n == n.parent.left &&
            nodeColor(n.sibling()) == NodeColor.BLACK &&
            nodeColor(n.sibling().left) == NodeColor.RED &&
            nodeColor(n.sibling().right) == NodeColor.BLACK)
        {
            n.sibling().color = NodeColor.RED;
            n.sibling().left.color = NodeColor.BLACK;
            rotateRight(n.sibling());
        }
        else if (n == n.parent.right &&
                 nodeColor(n.sibling()) == NodeColor.BLACK &&
                 nodeColor(n.sibling().right) == NodeColor.RED &&
                 nodeColor(n.sibling().left) == NodeColor.BLACK)
        {
            n.sibling().color = NodeColor.RED;
            n.sibling().right.color = NodeColor.BLACK;
            rotateLeft(n.sibling());
        }
        deleteCase6(n);
    }
    private void deleteCase6(RBNode n) {
        n.sibling().color = nodeColor(n.parent);
        n.parent.color = NodeColor.BLACK;
        if (n == n.parent.left) {
            assert nodeColor(n.sibling().right) == NodeColor.RED;
            n.sibling().right.color = NodeColor.BLACK;
            rotateLeft(n.parent);
        }
        else
        {
            assert nodeColor(n.sibling().left) == NodeColor.RED;
            n.sibling().left.color = NodeColor.BLACK;
            rotateRight(n.parent);
        }
    }
    public void print() {
        printHelper(root, 0);
    }

    private static void printHelper(RBNode n, int indent) {
        if (n == null) {
            System.out.print("<empty tree>");
            return;
        }
        if (n.right != null) {
            printHelper(n.right, indent + INDENT_STEP);
        }
        for (int i = 0; i < indent; i++)
            System.out.print(" ");
        if (n.color == NodeColor.BLACK)
            System.out.println(n.toString());
        else
            System.out.println("<" + n.toString() + ">");
        if (n.left != null) {
            printHelper(n.left, indent + INDENT_STEP);
        }
    }
    public class RBNode implements Comparable{
        public RBNode(Comparable _content){
            content = _content;
        }
        public void CopyContent(RBNode in){
            content = in.content;
        }
        public RBNode grandparent(){
            assert parent != null;
            assert parent.parent != null;
            return parent.parent;
        }
        public RBNode sibling(){
            assert parent != null;
            if(this == parent.left)
            {
                return parent.right;
            }
            else
            {
                return parent.left;
            }
        }
        public RBNode uncle(){
            assert parent != null;
            assert parent.parent != null;
            return parent.sibling();
        }
        public int compareTo(Object o) {
            RBNode in = (RBNode)o;
            return content.compareTo(in.content);
        }
        public String toString(){
            return content.toString();
        }
        public Comparable content;
        public RBNode left;
        public RBNode right;
        public RBNode parent;
        NodeColor color;
    }
    private static final int INDENT_STEP = 4;
    int compResult;

    
    public RBNode root;
    public long size = 0;
    private RBNode stash;
    public long created = 0;
    public Comparable smallest;
    
    private class RBNodeStash{
        public void requestNode(){
            if(root == null)
            {
                checkout = new RBNode(null);
            }
            else
            {
                checkout = root;
                root = root.right;
            }
        }
        public RBNode borrowNode(){
            return checkout;
        }
        public void returnRBNode(RBNode returned){
            returned.left = null;
            returned.right = null;
            returned.parent = null;
            if(root == null)
            {
                root = returned;
                last = root;
            }
            else
            {
                last.right = returned;
                last = returned;
            }
        }
        private RBNode root;
        private RBNode checkout;
        private RBNode last;
    }
    
}
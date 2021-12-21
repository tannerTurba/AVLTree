import java.io.*;
import java.util.*;

/*
 * Tanner Turba
 * November 10, 2021
 * Implements an AVL tree of ints (the keys) and fixed length character string fields
 * stored in a random access file.
 * Duplicate keys are not allowed. There will be at least 1 character string field.
 */
public class AVLTree {
    public RandomAccessFile f;
    private long root; //the address of the root node in the file
    private long free; //the address in the file of the first node in the free list
    private int numStringFields; //the number of fixed length character fields
    private int[] fieldLengths; //the length of each character field
    private int numIntFields; //the number of integer fields

    /**
     * A simple node class with additional attributes for an AVL tree
     */
    private class Node {
        private int key;
        private char[][] stringFields;
        private int[] intFields;
        private long left;
        private long right;
        private int height;

        /**
         * A standard constructor for an in memory node
         * @param l reference to left child
         * @param k the key value
         * @param r reference to right child
         * @param sFields character field to store strings
         * @param iFields int field to store ints
         */
        private Node(long l, int k, long r, char[][] sFields, int[] iFields) {
            left = l;
            key = k;
            right = r;
            stringFields = sFields;
            intFields = iFields;
            height = 0;
        }

        /**
         * constructor for a node that exists and is stored in the file
         * @param addr address of node stored in file
         * @throws IOException
         */
        private Node(long addr) throws IOException {
            //goto address and read key value
            f.seek(addr);
            key = f.readInt();

            //instantiate the 2D char array
            stringFields = new char[numStringFields][];
            for(int i = 0; i < numStringFields; i++)
                stringFields[i] = new char[fieldLengths[i]];

            //load String values into the 2D array stringFields, letter by letter
            for(int i = 0; i < numStringFields; i++) {
                for(int j = 0; j < fieldLengths[i]; j++) {
                    stringFields[i][j] = f.readChar();
                }
            }

            //load ints into the array intFields
            intFields = new int[numIntFields];
            for(int i = 0; i < numIntFields; i++) {
                intFields[i] = f.readInt();
            }

            //set remaining properties
            left = f.readLong();
            right = f.readLong();
            height = f.readInt();
        }

        //writes the node to the file at location addr
        private void writeNode(long addr) throws IOException {
            //goto address and write key value
            f.seek(addr);
            f.writeInt(key);

            //write chars to file and pad with null chars if necessary
            for(int i = 0; i < numStringFields; i++)
                for(int j = 0; j < fieldLengths[i]; j++)
                        f.writeChar(stringFields[i][j]);

            //write each element of intField to file
            for (int intField : intFields)
                f.writeInt(intField);

            //write remaining properties to file
            f.writeLong(left);
            f.writeLong(right);
            f.writeInt(height);
        }

        //overrides the toString() method for printing
        public String toString() {
            String result = "Key: " + key + "\n";

            result += "String Fields: ";
            for (char[] stringField : stringFields) {
                for (char c : stringField) {
                    if (c != '\0')
                        result += c;
                }
                result += ' ';
            }

            //write each element of intField to file
            result += "\nint Fields: ";
            for (int intField : intFields) result += intField + " ";

            result += '\n' + "Left: " + left + '\n' + "Right: " + right + '\n' + "Height: " + height;
            return result += '\n';
        }
    }

    /**
     * Creates a new empty AVL tree stored in the file fname.
     * The number of character string fields is stringFieldLengths.length.
     * stringFieldLengths contains the length of each string field.
     * @param fname name of new RandomAccessFile
     * @param stringFieldsLengths character field lengths
     * @param numIntFields2 number of int fields
     * @throws IOException
     */
    public AVLTree(String fname, int[] stringFieldsLengths, int numIntFields2) throws IOException {
        //create new RandomAccessFile that is allowed to read and write.
        //delete any existing files of the same name
        File path = new File(fname);
        if(path.exists())
            path.delete();

        //instantiate properties of AVLTree
        f = new RandomAccessFile(path, "rw");
        fieldLengths = stringFieldsLengths;
        numStringFields = stringFieldsLengths.length;
        numIntFields = numIntFields2;
        root = 0;
        free = 0;

        //write currently known values to the file
        f.seek(0);
        f.writeLong(root);
        f.writeLong(free);
        f.writeInt(numStringFields);
        for (int i : stringFieldsLengths)
            f.writeInt(i);

        f.writeInt(numIntFields);
    }

    /**
     * reuse an existing tree stored in the file fname
     * @param fname name of existing RandomAccessFile
     * @throws IOException
     */
    public AVLTree(String fname) throws IOException {
        //open existing file and instantiate values
        f = new RandomAccessFile(new File(fname), "rw");
        f.seek(0);
        root = f.readLong();
        free = f.readLong();
        numStringFields = f.readInt();
        fieldLengths = new int[numStringFields];
        for(int i = 0; i < numStringFields; i++)
            fieldLengths[i] = f.readInt();
        numIntFields = f.readInt();
    }

    /**
     * Insert k and the fields into the tree the string fields are null ('\0') padded.
     * If k is in the tree do nothing
     * PRE: the number and lengths of the sFields and iFields match the expected number and lengths
     * @param k the key value being inserted
     * @param sFields the char field being inserted
     * @param iFields the int field being inserted
     * @throws IOException
     */
    public void insert(int k, char[][] sFields, int[] iFields) throws IOException {
        root = insert(root, k, sFields, iFields);
    }

    /**
     * A private and recursive helper method to insert a new node into the file and tree.
     * Returns the address of root when done.
     * @param r the address of the current node
     * @param k the key value being inserted
     * @param sFields the char field being inserted
     * @param iFields the int field being inserted
     * @return the address of the inserted node
     * @throws IOException
     */
    private long insert(long r, int k, char[][] sFields, int[] iFields) throws IOException {
        Node node;

        //When the end of a leaf is reached, get a free address and add node.
        if(r == 0) {
            node = new Node(0, k, 0, sFields, iFields);
            long addr = removeFromFree();
            node.height = height(node);
            node.writeNode(addr);
            return addr;
        }

        node = new Node(r);
        //if the new key is less than the node's key, go left
        if(k < node.key) {
            node.left = insert(node.left, k, sFields, iFields);
        }
        //if the new key is greater than the node's key, go right
        else if(k > node.key) {
            node.right = insert(node.right, k, sFields, iFields);
        }
        //adjust height of the node(s) and rewrite it to the file.
        node.height = height(node);
        node.writeNode(r);
        return balance(r, node);
    }

    /**
     * print the contents of the nodes in the tree is ascending order of the key
     * do not print the null characters
     * @throws IOException
     */
    public void print() throws IOException {
        print(root);
    }

    /**
     * the private recursive helper method for the print() function
     * @param addr the address of the current node
     * @throws IOException
     */
    private void print(long addr) throws IOException {
        //if null, return
        if (addr == 0)
            return;

        //else print contents from smallest(left) to greatest(right)
        Node n = new Node(addr);
        print(n.left);
        System.out.println(n);
        print(n.right);
}

    /**
     * Used to get the char fields from a node that is associated with k.
     * @param k the key value being searched for
     * @return a LinkedList of Strings from the char field in the node with k.
     * @throws IOException
     */
    public LinkedList<String> stringFind(int k) throws IOException {
        Node current = new Node(root);

        //while the key hasn't been found...
        while( k != current.key) {
            //if k is less than the target, go left
            if (k < current.key && current.left != 0)
                current = new Node(current.left);

            //if k is greater than the target, go right
            else if (k > current.key && current.right != 0)
                current = new Node(current.right);

            //if you are at a leaf and still haven't found the target, return null
            else if(current.right == 0 || current.left == 0)
                return null;
        }
        //load values of the target node into the LinkedList
        LinkedList<String> list = new LinkedList<>();
        for(int i = 0; i < numStringFields; i++) {
            String str = "";
            for(int j = 0; j < fieldLengths[i] -1 ; j++ ) {
                //only add character if not null
                if(current.stringFields[i][j] == '\0') break;
                str += current.stringFields[i][j];
            }
            list.add(str);
        }
        return list;
    }

    /**
     * Used to get the int fields from a node that is associated with k.
     * @param k the key value being searched for
     * @return a LinkedList of Integers from the int field in the node with k.
     * @throws IOException
     */
    public LinkedList<Integer> intFind(int k) throws IOException {
        Node current = new Node(root);

        //while the target key hasn't been found...
        while( k != current.key) {
            //if lesser than the target, go left
            if (k < current.key && current.left != 0)
                current = new Node(current.left);

            //if greater than the target, go right
            else if (k > current.key && current.right != 0)
                current = new Node(current.right);

            //if target hasn't been found and at a leaf, return null
            else if(current.right == 0 || current.left == 0)
                return null;
        }
        //load int values into the LinkedList and return
        LinkedList<Integer> list = new LinkedList<>();
        for(int i = 0; i < numIntFields; i++) {
            list.add(current.intFields[i]);
        }
        return list;
    }

    /**
     * removes the node containing the key k
     * @param k the value being searched for
     * @throws IOException
     */
    public void remove(int k) throws IOException {
        root = remove(k, root);
    }

    /**
     * a private recursive helper method to remove a node
     * @param k the value being searched for
     * @param addr the address of the current node
     * @return the address of a node
     * @throws IOException
     */
    private long remove(int k, long addr) throws IOException {
        //if at a leaf, return
        if (addr == 0)
            return 0;
        Node current = new Node(addr);
        long retVal = addr;

        //if node found, do one of the following
        if (current.key == k) {
            //if both children are null, return a reference to null addr(0) and add node to free list
            if (current.left == 0 && current.right == 0) {
                addToFree(addr);
                return 0;
            }

            //if only the left child is null, return the addr of the only child and add node to free list
            else if(current.left == 0) {
                retVal = current.right;
                addToFree(addr);
                return retVal;
            }

            //if on the right child is null, return the addr of the only child and add node to free list
            else if (current.right == 0) {
                retVal = current.left;
                addToFree(addr);
                return retVal;
            }

            //else there are two children, so find a replacement and add node to free list
            else
                current.left = replace(current.left, current);
        }
        //go left if the key you're looking for is less than current node
        else if (current.key > k)
            current.left = remove(k, current.left);

        //go right if the key you're looking for is greater than current node
        else
            current.right = remove(k, current.right);

        //update height and rewrite node
        current.height = height(current);
        current.writeNode(retVal);

        //balance tree if heights are off and return
        if(heightDifference(addr) > 1 || heightDifference(addr) < -1)
            return balance(retVal, current);
        return retVal;
    }

    /**
     * Used to update the root node and free list in the RandomAccessFile if necessary.
     * Safely closes the file.
     * @throws IOException
     */
    public void close() throws IOException {
        f.seek(0);
        f.writeLong(root);
        f.writeLong(free);
        f.close();
    }

    /**
     * Used to determine the height of a specific node
     * @param n the specified node
     * @return the height of the node, or -1 if null.
     * @throws IOException
     */
    private int height(Node n) throws IOException {
        //if at a leaf, height is 0
        if(n.left == 0 && n.right == 0)
            return 0;
        Node left = null, right = null;

        //if left and/or right nodes exist, create in-memory versions
        if(n.left != 0)
            left = new Node(n.left);
        if(n.right != 0)
            right = new Node(n.right);

        //return 1 + the greatest existing height
        if(n.left == 0)
            return 1 + right.height;
        else if(n.right == 0)
            return 1 + left.height;
        return 1 + Math.max(left.height, right.height);
    }

    /**
     * Calculates the height difference of sibling nodes
     * @param addr the address of the current node
     * @return the height difference
     * @throws IOException
     */
    private int heightDifference(long addr) throws IOException {
        Node current = new Node(addr);
        Node left = null, right = null;
        int leftRet = 0, rightRet = 0;

        //if left node is not null, create in-memory version and get height
        if(current.left != 0) {
            left = new Node(current.left);
            if(left.height != 0)
                leftRet = left.height;
        }

        //if right node is not null, create in-memory version and get height
        if(current.right != 0) {
            right = new Node(current.right);
            if(right.height != 0)
                rightRet = right.height;
        }

        //if either are null, make correlating return value -1
        if(left == null)
            leftRet = -1;
        if(right == null)
            rightRet = -1;

        //return difference
        return leftRet - rightRet;
    }

    /**
     * rebalances AVL tree if needed and recalculates height of node.
     * @param addr the address of the current node
     * @param n the current node
     * @return the appropriate address
     * @throws IOException
     */
    private long balance(long addr, Node n) throws IOException {
        //if null, return a null reference
        if(addr == 0)
            return 0;

        //if the height of the left subtree is more than one greater than the right do one of the following
        if(heightDifference(addr) > 1) {
            //if left-left case, single rotate with left child
            if(heightDifference(n.left) > 0) {
                addr = rotateWithLeftChild(addr);
            }
            //else double rotate with left in left-right case
            else {
                addr = doubleWithLeftChild(addr);
            }
        }
        //if the height of the right subtree is more than one greater than the left do one of the following
        else if(heightDifference(addr) < -1) {
            //if right-right case, single rotate with right child
            if(heightDifference(n.right) < 0) {
                addr = rotateWithRightChild(addr);
            }
            //else double rotate with right in right-left case
            else {
                addr = doubleWithRightChild(addr);
            }
        }
        //else no rotations needed, so return
        return addr;
    }

    /**
     * Finds a suitable existing node to replace one that also exists.
     * @param addr address of the current node
     * @param old reference to the node being replaced
     * @return the appropriate address
     * @throws IOException
     */
    private long replace(long addr, Node old) throws IOException {
       Node current = new Node(addr);

        //if right is not null or at leaf, go right until at leaf
        if (current.right != 0){
            current.right = replace(current.right, old);
            current.writeNode(addr);
            //return the address of the balanced tree
            return addr;
        }
        //if at a left subtree's rightmost node, copy info into old node
        else{
            old.key = current.key;
            old.intFields = current.intFields;
            old.stringFields = current.stringFields;

            //update height, add node to free list, and return
            old.height = height(current);
            addToFree(addr);
            return balance(current.left, new Node(current.left));
        }
    }

    /**
     * Performs a single rotation with the left child.
     * @param addr address of the current node
     * @return appropriate address
     * @throws IOException
     */
    private long rotateWithLeftChild(long addr) throws IOException {
        Node current = new Node(addr);
        Node left = new Node(current.left);
        long retVal = current.left;

        //perform rotation by changing references
        current.left = left.right;
        left.right = addr;

        //recalculate heights, rewrite to file, and return a reference
        current.height = height(current);
        current.writeNode(addr);
        left.height = height(left);
        left.writeNode(retVal);
        return retVal;
    }

    /**
     * Performs a single rotation with the right child.
     * @param addr the address of the current node
     * @return the appropriate address
     * @throws IOException
     */
    private long rotateWithRightChild(long addr) throws IOException {
        Node current = new Node(addr);
        Node right = new Node(current.right);
        long retVal = current.right;

        //perform rotation by changing references
        current.right = right.left;
        right.left = addr;

        //recalculate heights, rewrite to file, and return a reference
        current.height = height(current);
        current.writeNode(addr);
        right.height = height(right);
        right.writeNode(retVal);
        return retVal;
    }

    /**
     * Performs a double rotation with the left child
     * @param addr the address of the current node
     * @return the appropriate address
     * @throws IOException
     */
    private long doubleWithLeftChild(long addr) throws IOException {
        //perform rotations
        Node current = new Node(addr);
        current.left = rotateWithRightChild(current.left);

        //recalculate height and rewrite node
        current.height = Math.max(new Node(current.left).height, new Node(current.right).height);
        current.writeNode(addr);

        //return a left rotation of the subtree
        return rotateWithLeftChild(addr);
    }

    /**
     * Performs a double rotation with the right child.
     * @param addr the address of the current node
     * @return the appropriate address
     * @throws IOException
     */
    private long doubleWithRightChild(long addr) throws IOException {
        //perform rotations
        Node current = new Node(addr);
        current.right = rotateWithLeftChild(current.right);

        //recalculate height and rewrite node
        current.height = Math.max(new Node(current.left).height, new Node(current.right).height);
        current.writeNode(addr);

        //return a right rotation of the subtree
        return rotateWithRightChild(addr);
    }

    /**
     * Adds an addr to the list of free addresses.
     * @param r the address being added to the free list
     * @throws IOException
     */
    private void addToFree(long r) throws IOException {
        long nextFree;
        //if the free list IS NOT empty, find the next free addr
        if(free != 0) {
            f.seek(free);
            nextFree = f.readLong();
        } else
            nextFree = 0;

        //update free list in file and memory
        free = r;
        f.seek(8);
        f.writeLong(free);
        f.seek(r);
        f.writeLong(nextFree);
    }

    /**
     * Removes the head of the free list and returns its addr for use.
     * @return the address to be used
     * @throws IOException
     */
    public long removeFromFree() throws IOException {
        long addr;

        //if the free list IS NOT empty, get head and set free to next addr in list
        if(free != 0) {
            addr = free;
            f.seek(free);
            free = f.readLong();
        }
        //else return the length, which is the next available addr
        else {
            addr = f.length();
            free = 0;
        }
        //update free list in file and memory
        f.seek(8);
        f.writeLong(free);
        return addr;
    }
}

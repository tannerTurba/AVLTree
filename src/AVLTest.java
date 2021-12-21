import java.io.*;
import java.util.*;
public class AVLTest {

    public AVLTest() {

    }

    public void test1(int nums[], int len) throws IOException {
        //this test does not require any rotations and only removes leaves
        System.out.println("Start test 1");
        int i;
        int sFieldLens[] = {10, 15};
        AVLTree a = new AVLTree("t1", sFieldLens, 2);
        char sFields[][] = new char[2][];
        int iFields[] = new int[2];
        for ( i = 0; i < len; i++) {
            sFields[0] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 10);
            sFields[1] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 15);
            iFields[0] = iFields[1] = nums[i];

            a.insert(nums[i], sFields, iFields);
        }

        System.out.println("Past inserts in test 1");
        a.print();
        System.out.println("Past first print in test 1");
        for ( i = len-1; i > 2; i--) {
            a.remove(nums[i]);
        }
        System.out.println("Past first removes in test 1");
        a.print();
        System.out.println("Past second print in test 1");
        a.close();
        a = new AVLTree("t1");
        System.out.println("Past close and reopen");
        a.print();
        a.remove(nums[2]);
        a.remove(nums[1]);
        a.remove(nums[0]);
        sFields[0] = Arrays.copyOf("Root".toCharArray(), 10);
        sFields[1] = Arrays.copyOf("Node Only".toCharArray(), 15);
        iFields[0] = iFields[1] = 999;
        a.insert(999, sFields, iFields);
        a.print();
        a.close();

    }

    public void test2(int nums[], int len) throws IOException {
        //this tests uses the same data used in test 1 but inserts and
        //removes in a different ordeer
        //The test will cause some rotations and will remove non-leaf node
        int i;
        System.out.println("\n\nStart test 2");
        int sFieldLens[] = {10, 15, 20};
        AVLTree a = new AVLTree("t2", sFieldLens, 1);
        char sFields[][] = new char[3][];
        int iFields[] = new int[1];
        for ( i = len-1; i >= 0; i--) {
            sFields[0] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 10);
            sFields[1] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 15);
            sFields[2] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 20);
            iFields[0] = nums[i];

            a.insert(nums[i], sFields, iFields);
        }

        System.out.println("Past inserts in test 2");
        a.print();
        System.out.println("Past first print in test 2");
        for ( i = len-1; i > 2; i--) {
            a.remove(nums[i]);
        }
        System.out.println("Past first removes in test 2");
        a.print();
        System.out.println("Past second print in test 2");
        a.close();
        a = new AVLTree("t2");
        System.out.println("Past close and reopen");
        a.print();
        a.remove(nums[2]);
        a.remove(nums[1]);
        a.remove(nums[0]);
        sFields[0] = Arrays.copyOf("Root".toCharArray(), 10);
        sFields[1] = Arrays.copyOf("Node".toCharArray(), 15);
        sFields[2] = Arrays.copyOf("Only".toCharArray(), 20);
        iFields[0] = 999;
        a.insert(999, sFields, iFields);
        a.print();
        a.close();

    }

    public void test3() throws IOException {
        //does inserts and removes using the data in the example I did in class
        //to run test3 you need the file in3.txt
        //in in3.txt lines that begin with a # are removes; all other lines are inserts
        System.out.println("Start test 3");
        BufferedReader b = new BufferedReader(new FileReader("in.txt"));
        String line;
        int numKeys = 0;
        int keys[] = new int[100];
        String fields[];
        int sFieldLens[] = {30, 30};
        char sFields[][] = new char[2][];
        int iFields[] = new int[3];
        AVLTree a = new AVLTree("t3", sFieldLens, 3);
        line = b.readLine();
        while (line != null) {
            fields = line.split(" ");
            if (fields[0].equals("#")) {
                a.remove(new Integer(fields[1]));
            } else {
                keys[numKeys] = new Integer(fields[0]);
                sFields[0] = Arrays.copyOf(fields[1].toCharArray(), 30);
                sFields[1] = Arrays.copyOf(fields[2].toCharArray(), 30);
                iFields[0] = new Integer(fields[3]);
                iFields[1] = new Integer(fields[4]);
                iFields[2] = new Integer(fields[5]);
                a.insert(keys[numKeys], sFields, iFields);
                numKeys++;
            }
            line = b.readLine();
        }
        System.out.println("numKeys: " + numKeys);
        a.print();
        for (int j = 0; j < numKeys; j++) {
            LinkedList<String> strs = a.stringFind(keys[j]);
            if (strs != null) {
                System.out.print(keys[j]+" "+strs.get(0) +" "+strs.get(1));
                LinkedList<Integer> nums = a.intFind(keys[j]); //since strs was not null nums should not be null
                System.out.println(" "+nums.get(0)+" "+nums.get(1)+" "+nums.get(2));
            }

        }
        a.close();
        a = new AVLTree("t3");
        a.print();
        a.close();
    }

    public void test4() throws IOException {
        int testSize = 2000;
        //insert 2000 random numbers, remove them all, inserts them again in reverse order and remove all but one of them.
        System.out.println("Start test 4");
        int i;
        int nums[] = new int[testSize];
        Random r = new Random(2017);
        for (i = 0; i < testSize; i++) {
            nums[i] = r.nextInt()% 100000;
        }
        System.out.println("array of randos created");
        int sFieldLens[] = {10, 15, 20, 30};
        AVLTree a = new AVLTree("t4", sFieldLens, 4);
        char sFields[][] = new char[4][];
        int iFields[] = new int[4];
        for ( i = 0; i < testSize; i++) {
            sFields[0] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 10);
            sFields[1] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 15);
            sFields[2] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 20);
            sFields[3] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 30);
            iFields[0] = iFields[1] = iFields[2] = iFields[3] = nums[i];

            a.insert(nums[i], sFields, iFields);
        }
        System.out.println("First inserts completed");
        for ( i = 0; i < testSize; i++) {
            a.remove(nums[i]);
        }
        System.out.println("First removes complete");
        for ( i = testSize - 1; i >= 0; i--) {
            sFields[0] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 10);
            sFields[1] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 15);
            sFields[2] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 20);
            sFields[3] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 30);
            iFields[0] = iFields[1] = iFields[2] = iFields[3] = nums[i];

            a.insert(nums[i], sFields, iFields);
        }
        System.out.println("Second inserts complete");
        for ( i = testSize - 1; i > 0; i--) {
            a.remove(nums[i]);
        }
        System.out.println("Second removes complete");
        a.close();
        a = new AVLTree("t4");
        a.print();
        a.close();
    }

    public static void main(String args[]) throws IOException {
        AVLTest test = new AVLTest();
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter the maximum value to use for tests 1 and 2: ");
        int max = scan.nextInt();
        System.out.print("Enter the depth of the tree for tests 1: ");
        int levels = scan.nextInt();
        int nums[] = new int[max];
        int start, increment, i;
        int j = 0;
        int divisor = 2;
        while (levels > 0) {
            start = max/divisor;
            increment = 2*start;
            for (i = start; i < max; i = i+increment) {
                nums[j] = i;
                j++;
            }
            divisor = divisor*2;
            levels--;
        }


        //As an example the data generated by the above code when 100 is the max and depth is 3
        //is 50, 25, 75, 12, 36, 60, 84
        //the depth should be less than log max (log base 2)
        test.test1(nums, j);
        System.out.println("-------------------------------------------");
        test.test2(nums, j);
        System.out.println("-------------------------------------------");
        test.test3();
        System.out.println("-------------------------------------------");
        test.test4();
    }

}

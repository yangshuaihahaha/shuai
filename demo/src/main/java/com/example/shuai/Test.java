package com.example.shuai;

public class Test {

    String str = "good";
    char[] ch = {'a', 'a', 'a', 'a', 'a'};

    public void exchange(String str, char[] ch) {
        str = "best";
        ch[0] = 'b';
    }

    public static void main(String[] args) {
        Test test = new Test();
        test.exchange(test.str, test.ch);
        System.out.println(test.str);
        System.out.println(test.ch);



    }
}

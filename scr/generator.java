package scr;
import java.security.SecureRandom;
import java.util.Scanner;
public class generator {
    private static final String Char_Set = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*_+";
    private static final SecureRandom RANDOM = new SecureRandom();
    public static String generators(int length){
        StringBuilder password = new StringBuilder();
        if (length<4){
            return "invalid length(length should be greater or equal than 4)";
        }
        int countnum = 0,countcap = 0, countlow = 0, countsign = 0;
        boolean flag = true;
        for (int i = 0; i < length; i++) {
            password.append(Char_Set.charAt(RANDOM.nextInt(Char_Set.length())));
        }
        //System.out.println(password);
        while(flag) {
            String Password = password.toString();
            for (char c : Password.toCharArray()) {
                if (Character.isDigit(c)){
                    countnum++;
                }
                else if (Character.isUpperCase(c)){
                    countcap++;
                }
                else if (Character.isLowerCase(c)){
                    countlow++;
                }
                else {
                    countsign++;
                }
                //System.out.println("num:"+countnum+"caps:"+countcap+"lows:"+countlow+"sign:"+countsign);

            }
            if (countnum == 0||countcap==0||countlow==0||countsign==0) {
                //System.out.println(password);
                password.setLength(0);
                for (int j = 0; j < length; j++){
                    password.append(Char_Set.charAt(RANDOM.nextInt(Char_Set.length())));
                }
                countnum = 0;
                countlow = 0;
                countcap = 0;
                countsign = 0;
                //System.out.println(password);
            } else {
                flag = false;
            }
        }
        return password.toString();
    }

    public static void main(String[] args) {
        Scanner input =  new Scanner(System.in);
        System.out.println("Please enter the length of the password: ");
        int length = input.nextInt();
        System.out.println("Your password is:");
        System.out.println(generators(length));

    }
}

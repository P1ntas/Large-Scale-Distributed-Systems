package frontend.gui;

import java.util.Scanner;


public class Interface{

    JSONReader reader;
    int option = -1;


    public Interface(){
        this.reader = new JSONReader();

        start();

    }

    //methods

    public void showShoppingLists(){
        //this.reader.info();
    }

    public void mainOption(){
        System.out.println("[1] Shopping Lists");
        System.out.println("Press any other key to quit!");
        System.out.println("Enter option:");
    }

    public void start(){
        Scanner scanner = new Scanner(System.in);

        while(true){
            mainOption();

            option = scanner.nextInt();

            switch(option){
                case 1:
                    showShoppingLists();
                    break;
                default:
                    option = -1;
                    break;
            }
            if(option == -1){
                break;
            }
        }
    }

}
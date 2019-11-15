package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;

import static java.awt.event.KeyEvent.VK_ENTER;

public class Chat extends JFrame{ //extends JFrame是因為大量使用JFrame類所以繼承比較方便
    private JPanel panel1;
    private JButton sendButton;
    private JTextField textField1;
    private JButton recordButton;
    private JButton cleanButton;
    private JButton quakeButton;
    private JTextArea messageTextArea;
    private JTextArea messageText;
    private JPanel southPanel;
    private JPanel centerPanel;
    private DatagramSocket socket = null;
    private BufferedWriter writer;

    public Chat() {
        init();//因為如果全部的東西都寫在構造方法中會很擁擠，所以分開寫
        event();
        Receive receive = new Receive();
        receive.start();

    }


    @Override
    protected void finalize() throws Throwable {
        socket.close();
        writer.close();
    }

    public void init(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(100,100,500,400);
        getContentPane().add(panel1);
        try{
            socket = new DatagramSocket();
            writer = new BufferedWriter(new FileWriter("record.txt",true));//true是追加功能，而不是清空寫入
        }catch (Exception e){
            System.out.println(e);
        }
        setVisible(true);
    }
    public void event(){
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    send();
                }catch(Exception e1){
                    System.out.println(e);
                }
            }
        });

        /**
         * 快捷鍵功能(Ctrl + Enter)，不用每次都用滑鼠點擊button才能寄出訊息
         */
        messageText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == VK_ENTER && e.isControlDown()){ //Ctrl + Enter同時成立
                    try{
                        send();
                    }catch(Exception e1){
                        System.out.println(e1);
                    }
                }
            }
        });

        recordButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    record();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        cleanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                messageTextArea.setText("");
            }
        });
        quakeButton.addActionListener(new ActionListener() { //震動對方
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    /**
                     * //選擇-1是為了有識別性，又不會再打字的過程中被打出來ex:'a'就有可能在聊天內容中有a
                     * 所以選-1不可能用打字的方式可以打出-1因為那是"-1"不是字節的-1
                     */
                    send(new byte[]{-1},textField1.getText());
                }catch (Exception e1){

                }
            }
        });
    }

    private void send(byte[] arr,String ip) throws IOException{ //將此功能從send()抽取出來，做成function重複利用
        DatagramPacket packet =
                new DatagramPacket(arr,arr.length, InetAddress.getByName(ip),7000);
        socket.send(packet);
    }

    private void send() throws IOException {
        String ip = textField1.getText();
        ip = (ip.trim().length() == 0) ? "255.255.255.255" : ip; //優化:如果IP為空的，那就是對所有人說用廣播位置 trim去頭尾的空白
        String message = messageText.getText();
        System.out.println(message);
        send(message.getBytes(),ip);
        message = "I say:" + message + "\n";
        messageTextArea.append(message);//這邊得用append 不然用setText會覆蓋掉之前的內容
        writer.write(message);
        messageText.setText("");  //訊息發送完後將輸入區的內容給清空

    }

    private void record() throws IOException{
        writer.flush(); //如果不刷新有些資料可能會在緩衝區中，在沒有調用close方法前不會寫入record.txt中，除非緩衝區滿了才會送出
        FileInputStream fis = new FileInputStream("record.txt");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len;
        byte[] arr = new byte[8192];
        while((len = fis.read(arr)) != -1){
            baos.write(arr,0,len);
        }
        String s = baos.toString();
        messageTextArea.setText(s); //在此不能用append，如果點兩次record就會出現兩次紀錄所以得用setText
    }

    private void quake() throws InterruptedException { //震動就是一直改變起始座標就好了
        int x = this.getLocation().x;
        int y = this.getLocation().y;
        for(int i = 2 ; i<5 ; i++){
            this.setLocation(x += 20, y += 20);
            Thread.sleep(30);
            this.setLocation(x += 20, y -= 20);
            Thread.sleep(30);
            this.setLocation(x -= 20, y += 20);
            Thread.sleep(30);
            this.setLocation(x -= 20, y -= 20);
            Thread.sleep(30);
        }


    }

    private class Receive extends Thread{  //在此定義成內部類比較好因為可以直接調用此類的一些方法
        @Override
        public void run() {
            try{
                DatagramSocket socketc = new DatagramSocket(7000);
                DatagramPacket packet = new DatagramPacket(new byte[1024],1024);
                while(true){
                    socketc.receive(packet);
                    byte[] arr = packet.getData();
                    if(arr[0] == -1 && packet.getLength() == 1){ //當接收到震動，就執行到震動
                        quake();
                        System.out.println("quake!!!!!!");
                        continue;
                    }
                    String s = new String(arr,0,packet.getLength());
                    s = new String(s.getBytes());
                    s = packet.getAddress() + ":" + s + "\n";
                    messageTextArea.append(s);
                    writer.write(s);
                    System.out.println(s);
                }
            }catch (Exception e1){
                System.out.println(e1);
            }
        }
    }

}

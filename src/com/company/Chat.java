package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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


    public Chat() {
        init();//因為如果全部的東西都寫在構造方法中會很擁擠，所以分開寫
        event();
        Receive receive = new Receive();
        receive.start();
    }


    @Override
    protected void finalize() throws Throwable {
        socket.close();
    }

    public void init(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(100,100,500,400);
        getContentPane().add(panel1);
        try{
            socket = new DatagramSocket();
        }catch (SocketException e){
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
        recordButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    public void send() throws SocketException, UnknownHostException {
        String message = messageText.getText();
        System.out.println(message);
        String ip = textField1.getText();
        DatagramPacket packet = new DatagramPacket(message.getBytes(),message.getBytes().length, InetAddress.getByName(ip),7000);
        messageTextArea.append("\nTim:" + message);//這邊得用append 不然用setText會覆蓋掉之前的內容
        messageText.setText("");  //訊息發送完後將輸入區的內容給清空

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
                    String s = new String(arr,0,packet.getLength());
                    s = new String(s.getBytes());
                    messageTextArea.append("\n" + packet.getAddress() + ":" + s);
                    System.out.println(s);
                }
            }catch (Exception e1){
                System.out.println(e1);
            }
        }
    }

}

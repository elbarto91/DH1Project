import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


public class Chat {
    public static final int PORT = 7777;
    private JButton submitButton;
    private JTextField messageToSend;
    private JComboBox comboBox1;
    private JPanel rootPanel;
    private JList list1;
    private JButton LOGOUTButton;
    private JTextArea textArea1;
    private JLabel usernameLabel;
    private String user;
    private Socket socket = null;
    private OutputStream os;
    private Writer wr;
    private PrintWriter prw;
    private InputStream is;
    private Reader rd;
    private BufferedReader brd;
    private Thread t;
    private JFrame frame;
    private String IpServer;
    private ThreadedEchoClient client;


    /**
     * @throws IOException
     */
    public Chat() throws IOException {
        checkUsername();
        this.submitButton.addActionListener( e -> sendMessege() );
        this.messageToSend.addKeyListener( new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessege();
                }
            }

        } );
        this.LOGOUTButton.addActionListener( e -> {
            logout();
        } );
    }

    public static void main(String[] args) throws IOException {
        new Chat();
    }

    /**
     * @return Ritorna un booleano che rappresenta la rieuscita o meno del login, controllando
     * la disponibilità dell'username utilizzato
     */
    public boolean login() {
        try {
            this.os = this.socket.getOutputStream();
            this.wr = new OutputStreamWriter( os, StandardCharsets.UTF_16 );
            this.prw = new PrintWriter( wr );
            this.prw.println( "<LOGIN>-" + this.user );
            this.prw.flush();
            this.is = this.socket.getInputStream();
            this.rd = new InputStreamReader( is, StandardCharsets.UTF_16 );
            this.brd = new BufferedReader( rd );
            String answer = this.brd.readLine();
            if (answer.equals( "<ACK>" )) {
                this.frame = new JFrame( "CHAT" );
                this.frame.setContentPane( rootPanel );
                this.frame.pack();
                this.messageToSend.addMouseListener( new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        messageToSend.setText( "" );
                    }
                } );
                this.frame.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
                this.frame.addWindowListener( new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        logout();
                    }
                } );
                this.frame.setVisible( true );
                this.usernameLabel.setText( "USERNAME : " + this.user );
                this.client = new ThreadedEchoClient( this.socket, textArea1, list1, this.user );
                this.t = new Thread( client );
                this.t.start();
                this.textArea1.setText( "SERVER: Ciao " + this.user + " benvenuto nella Chat :-)" );
                this.prw.println( "<UPDATEUSERLIST>" );
                this.prw.flush();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @throws IOException in caso di problemi di comunicazione
     *                     Viene richiamato in caso di riechiesta di chiusura della chat da parte dell'utente.
     *                     Sia attraverso il pulsante LOGOUT nella GUI sia attraverso il classico metodo di uscita('X' nella finestra).
     *                     Avvia una piccola finestra di conferma
     */
    public void logout() {
        int option = JOptionPane.showConfirmDialog(
                this.frame,
                "Sicuro di voler uscire dalla chat?",
                "Conferma chiusura",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE );
        if (option == JOptionPane.YES_OPTION) {
            this.prw.println( "<LOGOUT>-" + this.user );
            this.prw.flush();
            this.frame.dispose();
            closeConnection();
            client.stop();
        }
    }

    /**
     * Metodo utilizzato per chiudere il canale
     */
    private void closeConnection() {
        try {
            if (this.prw != null) {
                this.prw.close();
            }
            if (this.brd != null) {
                this.brd.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Metodo utilizzato per inviare messaggi al client, siano essi onetoone o broadcast(scelta effetuata attravrso controlli sulla GUI)
     */
    public void sendMessege() {
        if (!this.messageToSend.getText().equals( "" )) {
            if (this.comboBox1.getSelectedIndex() == 0 && this.list1.getSelectedIndex() != -1) {
                this.prw.println( "<ONETOONE>-<" + this.list1.getSelectedValue().toString() + ">-<" + this.messageToSend.getText() + ">" );
                this.prw.flush();
            } else if (this.comboBox1.getSelectedIndex() == 1) {
                this.prw.println( "<BROADCAST>-<" + this.user + ">-<" + this.messageToSend.getText() + ">" );
                this.prw.flush();

            }
            this.messageToSend.setText( "" );
        }
    }

    /**
     * @throws IOException in caso di errori di comunicazione
     *                     Metodo chaiamto alla creazione di un oggetto di tipo chat,esegue un controllo sia sull'username sia sull'indirizzo ip,
     *                     scelti dall'utente attraverso l'interfaccia grafica, in caso di ip o user non validi notifica all'utente il problema
     *                     e richarica la GUI di scelta
     */
    public void checkUsername() throws IOException {
        boolean userValid = false;
        boolean ipvalid = false;
        while (!userValid || !ipvalid) {
            userValid = false;
            ipvalid = false;
            JLabel label_login = new JLabel( "Inserisci username:" );
            JTextField login = new JTextField();
            JLabel label_ip = new JLabel( "Inserisci ip del server" );
            JTextField ip = new JTextField();
            ip.setText( "localhost" );
            Object[] array = {label_login, login, label_ip, ip};
            int res = JOptionPane.showConfirmDialog( null, array, "Login",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE );
            if (res == JOptionPane.OK_OPTION && !login.getText().trim().equals( "" ) && !ip.getText().equals( "" ) && login.getText().trim() != null && ip.getText().trim() != null) {
                String userTemp = login.getText().trim();
                this.IpServer = ip.getText();
                InetAddress address = InetAddress.getByName( this.IpServer );
                ipvalid = address.isReachable( 1000 );
                if (ipvalid) {
                    if (this.socket == null) {
                        this.socket = new Socket( this.IpServer, this.PORT );
                    }
                    this.user = userTemp;
                    userValid = login();
                    if (!userValid) {
                        JOptionPane.showMessageDialog( null, "User gia in uso" );
                    }
                } else {
                    JOptionPane.showMessageDialog( null, "Ip errato o server Offline" );
                }
            } else if (res == JOptionPane.CANCEL_OPTION || res == JOptionPane.CLOSED_OPTION) {
                if (this.socket != null) {
                    this.socket.close();
                }
                System.exit( 0 );

            }
        }

    }

}
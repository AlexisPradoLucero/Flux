import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.extensions.parsers.HEntity;
import gearth.extensions.parsers.HEntityType;
import gearth.extensions.parsers.HUserProfile;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.text.Text;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.nio.charset.StandardCharsets;

@ExtensionInfo(
        Title = "Flux",
        Description = "Multiple tools",
        Version = "1.0",
        Author = "AlexisPrado"
)

public class Flux extends ExtensionForm {

    public CheckBox UserClipboard;
    public Text YourName;
    public Text YourRespects;
    public Text YourRewardPoints;
    public Text YourGender;
    public CheckBox AntiKick;
    public CheckBox AntiLookPoint;
    public CheckBox AntiTrade;
    public CheckBox NoTyping;
    public CheckBox FakeRespects;
    public CheckBox ClickThrough;
    public CheckBox AntiAfk;
    public CheckBox Grinding;
    public CheckBox Derp;
    public CheckBox UserClipboardExt;
    public CheckBox UserGoogle;
    public CheckBox UserGoogleExt;
    public CheckBox UserWhatsMyName;
    public CheckBox UserWhatsMyNameExt;
    public CheckBox UserIDCrawl;
    public CheckBox UserIDCrawlExt;
    private String Name;
    private String Look;
    private String Gender;
    private int Respects;
    private int Points;
    private int RoomID;
    private int ID;
    TreeMap<Integer, Integer> UserIdAndIndex = new TreeMap<>();
    TreeMap<Integer, String> UserIdAndName = new TreeMap<>();
    private int UserId = -1;
    private Thread antiafkthread;
    private Thread grindingthread;
    private Thread derpthread;

    @Override
    protected void initExtension() {
        sendToServer(new HPacket("InfoRetrieve", HMessage.Direction.TOSERVER));
        intercept(HMessage.Direction.TOCLIENT, "UserObject", this::InUserObject);
        intercept(HMessage.Direction.TOCLIENT, "AchievementsScore", this::InAchievementsScore);
        intercept(HMessage.Direction.TOSERVER, "OpenFlatConnection", this::OutOpenFlatConnection);
        intercept(HMessage.Direction.TOCLIENT, "GenericError", this::InGenericError);
        intercept(HMessage.Direction.TOSERVER, "LookTo", this::OutLookTo);
        intercept(HMessage.Direction.TOCLIENT, "TradingOpen", this::InTradingOpen);
        intercept(HMessage.Direction.TOCLIENT, "TradingClose", this::InTradingClose);
        intercept(HMessage.Direction.TOSERVER, "StartTyping", this::OutStartTyping);
        intercept(HMessage.Direction.TOCLIENT, "RespectNotification", this::InRespectNotification);
        intercept(HMessage.Direction.TOCLIENT, "Users", this::InUsers);
        intercept(HMessage.Direction.TOSERVER, "GetSelectedBadges", this::OutGetSelectedBadges);
        intercept(HMessage.Direction.TOCLIENT, "ExtendedProfile", this::InExtendedProfile);
    }

    private void InExtendedProfile(HMessage hMessage) {
        HPacket hPacket = hMessage.getPacket();
        try {
            HUserProfile userProfile = new HUserProfile(hPacket);
            String usernameprofile = userProfile.getUsername();
            System.out.println(usernameprofile);

            if (UserClipboardExt.isSelected()) {
                // Copiar al portapapeles
                StringSelection selection = new StringSelection(usernameprofile);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                sendToClient(new HPacket("IssueCloseNotification", HMessage.Direction.TOCLIENT, 1, "User copied to clipboard: " + usernameprofile));
                System.out.println("Text copied to clipboard: " + usernameprofile);
            }

            if (UserGoogleExt.isSelected()) {
                try {
                    String enlace = "https://www.google.com/search?q=" + URLEncoder.encode('"' + usernameprofile + '"', StandardCharsets.UTF_8.toString());

                    // Abrir el enlace en el navegador predeterminado
                    Desktop.getDesktop().browse(new URI(enlace));
                    System.out.println("Enlace abierto en el navegador: " + enlace);
                    sendToClient(new HPacket("IssueCloseNotification", HMessage.Direction.TOCLIENT, 1, "You have opened the link " + enlace + " in the browser."));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Error al abrir el enlace en el navegador.");
                }
            }

            if (UserWhatsMyNameExt.isSelected()) {
                // Construir el enlace con el nombre de usuario
                String enlace = "https://whatsmyname.app/?q=" + usernameprofile;

                // Abrir el enlace en el navegador predeterminado
                try {
                    Desktop.getDesktop().browse(new URI(enlace));
                    System.out.println("Enlace abierto en el navegador: " + enlace);
                    sendToClient(new HPacket("IssueCloseNotification", HMessage.Direction.TOCLIENT, 1, "You have opened the link " + enlace + " in the browser."));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Error al abrir el enlace en el navegador.");
                }
            }

            if (UserIDCrawlExt.isSelected()) {
                // Construir el enlace con el nombre de usuario
                String enlace = "https://www.idcrawl.com/u/" + usernameprofile;

                // Abrir el enlace en el navegador predeterminado
                try {
                    Desktop.getDesktop().browse(new URI(enlace));
                    System.out.println("Enlace abierto en el navegador: " + enlace);
                    sendToClient(new HPacket("IssueCloseNotification", HMessage.Direction.TOCLIENT, 1, "You have opened the link " + enlace + " in the browser."));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Error al abrir el enlace en el navegador.");
                }
            }

        } catch (Exception e) {
        }
    }

    private void OutGetSelectedBadges(HMessage hMessage) {
        UserId = hMessage.getPacket().readInteger();
        try {
            String user = UserIdAndName.get(UserId);
            if (!user.equals("null")) {
                System.out.println(user);

                if (UserClipboard.isSelected()) {
                    // Copiar al portapapeles
                    StringSelection selection = new StringSelection(user);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                    sendToClient(new HPacket("Chat", HMessage.Direction.TOCLIENT, -1, "User copied to clipboard: " + user, 0, 30, 0, 0));
                    System.out.println("Text copied to clipboard: " + user);
                }

                if (UserGoogle.isSelected()) {
                    // Construir el enlace con el nombre de usuario codificado
                    try {
                        String enlace = "https://www.google.com/search?q=" + URLEncoder.encode('"' + user + '"', StandardCharsets.UTF_8.toString());

                        // Abrir el enlace en el navegador predeterminado
                        Desktop.getDesktop().browse(new URI(enlace));
                        System.out.println("Enlace abierto en el navegador: " + enlace);
                        sendToClient(new HPacket("Chat", HMessage.Direction.TOCLIENT, -1, "You have opened the link " + enlace + " in the browser.", 0, 30, 0, 0));
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Error al abrir el enlace en el navegador.");
                    }
                }

                if (UserWhatsMyName.isSelected()) {
                    // Construir el enlace con el nombre de usuario
                    String enlace = "https://whatsmyname.app/?q=" + user;

                    // Abrir el enlace en el navegador predeterminado
                    try {
                        Desktop.getDesktop().browse(new URI(enlace));
                        System.out.println("Enlace abierto en el navegador: " + enlace);
                        sendToClient(new HPacket("Chat", HMessage.Direction.TOCLIENT, -1, "You have opened the link " + enlace + " in the browser.", 0, 30, 0, 0));
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Error al abrir el enlace en el navegador.");
                    }
                }

                if (UserIDCrawl.isSelected()) {
                    // Construir el enlace con el nombre de usuario
                    String enlace = "https://www.idcrawl.com/u/" + user;

                    // Abrir el enlace en el navegador predeterminado
                    try {
                        Desktop.getDesktop().browse(new URI(enlace));
                        System.out.println("Enlace abierto en el navegador: " + enlace);
                        sendToClient(new HPacket("Chat", HMessage.Direction.TOCLIENT, -1, "You have opened the link " + enlace + " in the browser.", 0, 30, 0, 0));
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Error al abrir el enlace en el navegador.");
                    }
                }
            }
        } catch (NullPointerException ignored) {
        }
    }

    private void InUsers(HMessage hMessage) {
        try {
            HPacket hPacket = hMessage.getPacket();
            HEntity[] roomUsersList = HEntity.parse(hPacket);
            for (HEntity hEntity : roomUsersList) {
                if (hEntity.getEntityType().equals(HEntityType.HABBO)) {
                    if (!UserIdAndIndex.containsKey(hEntity.getId())) {
                        UserIdAndIndex.put(hEntity.getId(), hEntity.getIndex());
                        UserIdAndName.put(hEntity.getId(), hEntity.getName());
                    } else {
                        UserIdAndIndex.replace(hEntity.getId(), hEntity.getIndex());
                        UserIdAndName.replace(hEntity.getId(), hEntity.getName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void InRespectNotification(HMessage hMessage) {
        if (FakeRespects.isSelected()) {
            {
                int ID_User = hMessage.getPacket().readInteger();
                {
                    if (ID_User != ID) {
                        sendToServer(new HPacket("AvatarExpression", HMessage.Direction.TOSERVER, 7));
                    }
                }
            }
        }
    }

    private void OutStartTyping(HMessage hMessage) {
        if (NoTyping.isSelected()) {
            hMessage.setBlocked(true);
        }
    }

    private void InTradingClose(HMessage hMessage) {
        if (AntiTrade.isSelected()) {
            hMessage.setBlocked(true);
        }
    }

    private void InTradingOpen(HMessage hMessage) {
        if (AntiTrade.isSelected()) {
            hMessage.setBlocked(true);
            sendToServer(new HPacket("CloseTrading", HMessage.Direction.TOSERVER));
        }
    }

    private void OutLookTo(HMessage hMessage) {
        if (AntiLookPoint.isSelected()) {
            hMessage.setBlocked(true);
        }
    }

    private void InGenericError(HMessage hMessage) {
        if (AntiKick.isSelected()) {
            sendToServer(new HPacket("GetGuestRoom", HMessage.Direction.TOSERVER, RoomID, 0, 1));
        }
    }

    private void OutOpenFlatConnection(HMessage hMessage) {
        RoomID = hMessage.getPacket().readInteger();
        UserIdAndIndex.clear();
        UserIdAndName.clear();
        UserId = -1;
    }

    private void InAchievementsScore(HMessage hMessage) {
        Points = hMessage.getPacket().readInteger();
        Platform.runLater(() -> YourRewardPoints.setText("Your reward points: " + Points));
    }

    private void InUserObject(HMessage hMessage) {
        ID = hMessage.getPacket().readInteger();
        Name = hMessage.getPacket().readString();
        Look = hMessage.getPacket().readString();
        Gender = hMessage.getPacket().readString();
        hMessage.getPacket().readString();
        hMessage.getPacket().readString();
        hMessage.getPacket().readBoolean();
        Respects = hMessage.getPacket().readInteger();
        Platform.runLater(() -> YourName.setText("Your username is: " + Name));
        Platform.runLater(() -> YourGender.setText("Your gender: " + Gender.replace("M", "Male").replace("F", "Female")));
        Platform.runLater(() -> YourRespects.setText("Your respects: " + Respects));
    }

    public void handleClickThrough() {
        if (ClickThrough.isSelected()) {
            sendToClient(new HPacket("YouArePlayingGame", HMessage.Direction.TOCLIENT, true));  // Enable Click Through
        } else {
            sendToClient(new HPacket("YouArePlayingGame", HMessage.Direction.TOCLIENT, false)); // Disable Click Through
        }
    }

    public void restoreoriginallooklistener() {
        sendToServer(new HPacket("UpdateFigureData", HMessage.Direction.TOSERVER, Gender, Look));
    }

    public void handleantiafk() {
        if (AntiAfk.isSelected()) {
            antiafkthread = new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(15000);
                        sendToServer(new HPacket("AvatarExpression", HMessage.Direction.TOSERVER, 0));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            antiafkthread.start();
        } else {
            if (antiafkthread != null) {
                antiafkthread.stop();
            }
        }
    }

    public void handlegrinding() {
        if (Grinding.isSelected()) {
            grindingthread = new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(100);
                        Random sign = new Random();
                        int N = sign.nextInt(17);
                        sendToServer(new HPacket("Sign", HMessage.Direction.TOSERVER, N));

                        Thread.sleep(100);
                        sendToServer(new HPacket("ChangePosture", HMessage.Direction.TOSERVER, 1));

                        Thread.sleep(100);
                        sendToServer(new HPacket("AvatarExpression", HMessage.Direction.TOSERVER, 5));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            grindingthread.start();
        } else {
            if (grindingthread != null) {
                grindingthread.stop();
            }
        }
    }

    public void handlederp() {
        if (Derp.isSelected()) {
            derpthread = new Thread(() -> {
                try {
                    while (true) {
                        sendToServer(new HPacket("LookTo", HMessage.Direction.TOSERVER, 1000, -10000));   // NE
                        Thread.sleep(100);
                        sendToServer(new HPacket("LookTo", HMessage.Direction.TOSERVER, 10000, -1000));   // E
                        Thread.sleep(150);
                        sendToServer(new HPacket("LookTo", HMessage.Direction.TOSERVER, 10000, 1000));    // SE
                        Thread.sleep(200);
                        sendToServer(new HPacket("LookTo", HMessage.Direction.TOSERVER, 1000, 10000));    // S
                        Thread.sleep(250);
                        sendToServer(new HPacket("LookTo", HMessage.Direction.TOSERVER, -1000, 10000));   // SW
                        Thread.sleep(300);
                        sendToServer(new HPacket("LookTo", HMessage.Direction.TOSERVER, -10000, 1000));   // W
                        Thread.sleep(350);
                        sendToServer(new HPacket("LookTo", HMessage.Direction.TOSERVER, -10000, -1000));  // NW
                        Thread.sleep(400);
                        sendToServer(new HPacket("LookTo", HMessage.Direction.TOSERVER, -1000, -10000));  // N
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            derpthread.start();
        } else {
            if (derpthread != null) {
                derpthread.stop();
            }
        }
    }
}
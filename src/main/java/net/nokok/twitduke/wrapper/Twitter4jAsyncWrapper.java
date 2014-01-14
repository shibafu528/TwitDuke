package net.nokok.twitduke.wrapper;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import net.nokok.twitduke.controller.MainViewController;
import net.nokok.twitduke.model.ConsumerKey;
import net.nokok.twitduke.model.TwitterListenerImpl;
import net.nokok.twitduke.model.account.AccessTokenManager;
import net.nokok.twitduke.util.URLUtil;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class Twitter4jAsyncWrapper {

    private long replyId = 0L;

    private static final AsyncTwitter       asynctwitter = AsyncTwitterFactory.getSingleton();
    private final        AccessTokenManager tokenManager = AccessTokenManager.getInstance();
    private MainViewController mainViewController;

    private static final Twitter4jAsyncWrapper instance = new Twitter4jAsyncWrapper();

    private Twitter4jAsyncWrapper() {
        asynctwitter.setOAuthConsumer(ConsumerKey.TWITTER_CONSUMER_KEY, ConsumerKey.TWITTER_CONSUMER_SECRET);
        if (tokenManager.isAuthenticated()) {
            asynctwitter.setOAuthAccessToken(tokenManager.readPrimaryAccount());
        } else {
            OAuthDialog dialog = new OAuthDialog();
            dialog.setVisible(true);
            dialog.setAlwaysOnTop(true);
        }
    }

    public static Twitter4jAsyncWrapper getInstance() {
        return instance;
    }

    public void setView(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }

    public void enableTwitterListener() {
        asynctwitter.addListener(new TwitterListenerImpl(mainViewController));
    }

    public void replyTweet(StatusUpdate status) {
        asynctwitter.updateStatus(status.inReplyToStatusId(replyId));
        replyId = 0;
    }

    public void replyPreprocess(Status status) {
        this.replyId = status.getId();
        mainViewController.setReply(status.getUser().getScreenName());
    }

    public void favoriteTweet(long statusId) {
        asynctwitter.createFavorite(statusId);
    }

    public void removeFavoriteTweet(long statusId) {
        asynctwitter.destroyFavorite(statusId);
    }

    public void retweetTweet(long statusId) {
        asynctwitter.retweetStatus(statusId);
    }

    public void deleteTweet(long statusId) {
        asynctwitter.destroyStatus(statusId);
    }

    public void sendTweet(String text) {
        if (replyId != 0) {
            replyTweet(new StatusUpdate(text));
        }
        asynctwitter.updateStatus(text);
    }

    public void getHomeTimeLine() {
        asynctwitter.getHomeTimeline();
    }

    public void getMentions() {
        asynctwitter.getMentions();
    }

    public void getUserInfomation(long userId) {
        long[] users = new long[]{userId};
        asynctwitter.lookupUsers(users);
    }


    class OAuthDialog extends JDialog {

        private final JTextField textField = new JTextField("");
        private final JButton    okButton  = new JButton("表示されたPINを入力後クリック");

        public OAuthDialog() {
            this.setLayout(new BorderLayout());
            this.setTitle("認証してください");
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            this.add(textField, BorderLayout.NORTH);
            this.add(okButton, BorderLayout.SOUTH);

            try {
                final RequestToken requestToken = asynctwitter.getOAuthRequestToken();

                okButton.addActionListener(e -> {
                    try {
                        okButtonClicked(requestToken);
                    } catch (TwitterException ex) {
                        ex.printStackTrace();
                    }
                });

                URLUtil.openInBrowser(requestToken.getAuthenticationURL());

            } catch (TwitterException e) {
                e.printStackTrace();
            }
            this.pack();
        }

        private void okButtonClicked(RequestToken requestToken) throws TwitterException {
            this.setTitle("認証処理/設定書き込み中");
            AccessToken token = asynctwitter.getOAuthAccessToken(requestToken, textField.getText());
            asynctwitter.setOAuthAccessToken(token);
            tokenManager.createTokenDirectory();
            tokenManager.writeAccessToken(token);
            this.dispose();
        }
    }
}

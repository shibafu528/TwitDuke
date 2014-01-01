package net.nokok.twitduke.wrapper;

import net.nokok.twitduke.model.AccessTokenManager;
import net.nokok.twitduke.model.ConsumerKey;
import net.nokok.twitduke.model.TweetCellFactory;
import net.nokok.twitduke.view.MainView;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Twitter4jAsyncWrapper {

    private long replyId = 0L;

    private static final AsyncTwitter       twitter      = AsyncTwitterFactory.getSingleton();
    private final        AccessTokenManager tokenManager = AccessTokenManager.getInstance();
    private final        TweetCellFactory   factory      = new TweetCellFactory(this);
    private MainView          mainView;
    private UserStreamAdapter userStream;

    public Twitter4jAsyncWrapper() {
        twitter.setOAuthConsumer(ConsumerKey.TWITTER_CONSUMER_KEY, ConsumerKey.TWITTER_CONSUMER_SECRET);
        if (tokenManager.isAuthenticated()) {
            twitter.setOAuthAccessToken(tokenManager.readPrimaryAccount());
        } else {
            OAuthDialog dialog = new OAuthDialog();
            dialog.setVisible(true);
            dialog.setAlwaysOnTop(true);
        }
    }

    public UserStreamAdapter getUserStream() {
        return userStream;
    }

    public void setView(MainView mainView) {
        this.mainView = mainView;
        if (userStream == null) {
            userStream = new MyUserStreamAdapter(mainView);
        }
    }

    public void replyTweet(StatusUpdate status) {
        twitter.updateStatus(status.inReplyToStatusId(replyId));
        replyId = 0;
    }

    public void replyPreprocess(Status status) {
        this.replyId = status.getId();
        mainView.setTweetTextField("@" + status.getUser().getScreenName() + " ");
    }

    public void favoriteTweet(long statusId) {
        twitter.createFavorite(statusId);
    }

    public void removeFavoriteTweet(long statusId) {
        twitter.destroyFavorite(statusId);
    }

    public void retweetTweet(long statusId) {
        twitter.retweetStatus(statusId);
    }

    public void deleteTweet(long statusId) {
        twitter.destroyStatus(statusId);
    }

    public void sendTweet(String text) {
        if (replyId != 0) {
            replyTweet(new StatusUpdate(text));
        }
        twitter.updateStatus(text);
    }

    class MyUserStreamAdapter extends UserStreamAdapter {

        private MainView view;

        public MyUserStreamAdapter(MainView view) {
            this.view = view;
        }

        @Override
        public void onStatus(Status status) {
            view.insertTweetCell(factory.createTweetCell(status));
        }
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
                final RequestToken requestToken = twitter.getOAuthRequestToken();

                try {
                    Desktop.getDesktop().browse(new URI(requestToken.getAuthenticationURL()));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                    throw new InternalError("認証URLオープンエラー");
                }

                textField.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {

                    }
                });
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            okButtonClicked(requestToken);
                        } catch (TwitterException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

            } catch (TwitterException e) {
                e.printStackTrace();
            }
            this.pack();
        }

        private void okButtonClicked(RequestToken requestToken) throws TwitterException {
            this.setTitle("認証処理/設定書き込み中");
            AccessToken token = twitter.getOAuthAccessToken(requestToken, textField.getText());
            twitter.setOAuthAccessToken(token);
            tokenManager.writeAccessToken(token);
            this.dispose();
        }
    }
}

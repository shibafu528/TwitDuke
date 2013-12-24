package net.nokok.twitduke.wrapper;

import net.nokok.twitduke.model.AccessTokenManager;
import net.nokok.twitduke.model.ConsumerKey;
import net.nokok.twitduke.util.TweetCellFactory;
import net.nokok.twitduke.view.MainView;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Twitter4jAsyncWrapper {

    private final        AccessTokenManager tokenManager = AccessTokenManager.getInstance();
    private static final AsyncTwitter       twitter      = AsyncTwitterFactory.getSingleton();

    private UserStreamAdapter userStream;

    public Twitter4jAsyncWrapper() {
        twitter.setOAuthConsumer(ConsumerKey.TWITTER_CONSUMER_KEY, ConsumerKey.TWITTER_CONSUMER_SECRET);
        if (tokenManager.isAuthenticated()) {
            twitter.setOAuthAccessToken(tokenManager.readPrimaryAccount());
        } else {
            OAuthDialog dialog = new OAuthDialog();
            dialog.setVisible(true);
        }
    }

    public UserStreamAdapter getUserStream() {
        return userStream;
    }

    public void setView(MainView mainView) {
        if (userStream == null) {
            userStream = new MyUserStreamAdapter(mainView);
        }
    }

    public void sendTweet(String text) {
        twitter.updateStatus(text);
    }

    class MyUserStreamAdapter extends UserStreamAdapter {

        private MainView view;

        public MyUserStreamAdapter(MainView view) {
            this.view = view;
        }

        @Override
        public void onStatus(Status status) {
            view.insertTweetCell(TweetCellFactory.createTweetCell(status));
        }
    }

    class OAuthDialog extends JDialog {

        private final JTextField textField = new JTextField("表示されたPINを入力後OKボタンをクリック");
        private final JButton    okButton  = new JButton("OK");

        public OAuthDialog() {
            this.setLayout(new BorderLayout());
            this.setTitle("認証してください");
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            this.add(textField, BorderLayout.NORTH);
            this.add(okButton, BorderLayout.SOUTH);
            this.setMinimumSize(new Dimension(350, 80));

            try {
                final RequestToken requestToken = twitter.getOAuthRequestToken();

                try {
                    Desktop.getDesktop().browse(new URI(requestToken.getAuthenticationURL()));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                    throw new InternalError("認証URLオープンエラー");
                }
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
        }

        private void okButtonClicked(RequestToken requestToken) throws TwitterException {
            AccessToken token = twitter.getOAuthAccessToken(requestToken, textField.getText());
            System.out.println(token);
            twitter.setOAuthAccessToken(token);
            tokenManager.writeAccessToken(token);
            this.dispose();
        }
    }
}

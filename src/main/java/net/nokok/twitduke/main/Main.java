package net.nokok.twitduke.main;

import net.nokok.twitduke.controller.MainViewController;
import net.nokok.twitduke.model.TweetCellManager;
import net.nokok.twitduke.model.account.AccessTokenManager;
import net.nokok.twitduke.model.impl.NotificationListenerImpl;
import net.nokok.twitduke.model.impl.RateLimitStatusListenerImpl;
import net.nokok.twitduke.model.impl.UserStreamListenerImpl;
import net.nokok.twitduke.model.listener.NotificationListener;
import net.nokok.twitduke.model.listener.TweetCellUpdateListener;
import net.nokok.twitduke.model.thread.FileCreateWatcher;
import net.nokok.twitduke.model.thread.IFileWatcher;
import net.nokok.twitduke.wrapper.Twitter4jAsyncWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import twitter4j.ConnectionLifeCycleListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStreamListener;

public class Main implements IFileWatcher {

    public static final Log logger = LogFactory.getLog(Main.class);

    private Twitter4jAsyncWrapper   wrapper;
    private MainViewController      mainViewController;
    private NotificationListener    notificationListener;
    private TwitterStream           twitterStream;
    private TweetCellUpdateListener tweetCellUpdateListener;

    /**
     * TwitDukeのエントリーポイントです
     * 渡された全てのオプションは無視されます
     *
     * @param args 渡されたオプションが格納された配列
     */
    public static void main(String[] args) {
        new Main().boot();
    }

    /**
     * TwitDukeの起動処理を行います
     * アクセストークンファイルの監視を開始します
     */
    private void boot() {
        logger.info("起動処理を開始します");

        readConfigFiles();
        mainViewInitialize();
        notificationListener = new NotificationListenerImpl(mainViewController);
        tweetCellUpdateListener = new TweetCellManager();
        twitterAPIWrapperInitialize();
        String accessTokenFilePath = AccessTokenManager.getInstance().getTokenFileListPath();
        new FileCreateWatcher(accessTokenFilePath, this).start();

        logger.info("起動処理が完了しました");
    }

    /**
     * 設定ファイルの読み込みと再設定を行います
     */
    private void readConfigFiles() {/*TODO:設定画面の実装時に追加する*/}

    /**
     * MainViewControllerの初期化を行います
     */
    private void mainViewInitialize() {
        mainViewController = new MainViewController();
    }

    /**
     * TwitterAPIWrapperの初期化を行います
     */
    private void twitterAPIWrapperInitialize() {
        twitterStream = TwitterStreamFactory.getSingleton();
        connectionLifeCycleListenerInitialize(twitterStream);
        twitterStream.addRateLimitStatusListener(new RateLimitStatusListenerImpl(notificationListener));

        logger.info("TwitterAPIラッパーの初期化を開始します");

        wrapper = Twitter4jAsyncWrapper.getInstance();
        wrapper.setNotificationListener(notificationListener);
        wrapper.setCellInsertionListener(mainViewController);
        wrapper.setReplyListener(mainViewController);
        wrapper.enableTwitterListener();
        twitterStream.setOAuthConsumer(Config.TWITTER_CONSUMER_KEY, Config.TWITTER_CONSUMER_SECRET);
        UserStreamListener userStreamListener = userStreamListenerInitialize();
        twitterStream.addListener(userStreamListener);
    }

    /**
     * TwitterStreamに接続状態を監視するリスナをセットします
     *
     * @param twitterStream リスナをセットするTwitterStream
     */
    private void connectionLifeCycleListenerInitialize(TwitterStream twitterStream) {
        twitterStream.addConnectionLifeCycleListener(new ConnectionLifeCycleListener() {
            @Override
            public void onConnect() {
                String message = "UserStreamに接続しました";
                notificationListener.setNotification(message);
                mainViewController.launchTitleAnimation();

                logger.info(message);
            }

            @Override
            public void onDisconnect() {
                String message = "UserStreamとの接続が切れました";
                notificationListener.setNotification(message);

                logger.warn(message);
            }

            @Override
            public void onCleanUp() {
                String message = "UserStream一時停止";
                notificationListener.setNotification(message);

                logger.info(message);
            }
        });
    }

    /**
     * UserStreamListenerの初期化を行います
     */
    private UserStreamListener userStreamListenerInitialize() {
        logger.info("UserStreamListenerの初期化をします");

        UserStreamListenerImpl userStreamListener = new UserStreamListenerImpl();
        userStreamListener.setCellInsertionListener(mainViewController);
        userStreamListener.setNotificationListener(notificationListener);
        userStreamListener.setTweetCellUpdateListener(tweetCellUpdateListener);
        return userStreamListener;
    }

    /**
     * 認証ファイルが書き込まれたらFileCreateWatcherによって呼ばれます
     *
     * @see net.nokok.twitduke.model.thread.FileCreateWatcher
     */
    @Override
    public void filesCreated() {
        logger.info("認証ファイルを見つけました");

        startUserStream();
        fetchTimelines();
    }

    /**
     * UserStreamの受信を開始します。
     */
    private void startUserStream() {
        logger.info("UserStreamの受信を開始します");

        mainViewController.start(wrapper, notificationListener, tweetCellUpdateListener);
        twitterStream.setOAuthAccessToken(AccessTokenManager.getInstance().readPrimaryAccount());
        twitterStream.user();
        notificationListener.setNotification("TwitDuke " + Config.VERSION);
    }

    /**
     * TwitterAPIからタイムラインを非同期で取得します
     */
    private void fetchTimelines() {
        logger.info("タイムラインを取得します");

        wrapper.getMentions();
        wrapper.getHomeTimeLine();
    }
}

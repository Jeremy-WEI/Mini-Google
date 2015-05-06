package cis555.searchengine;

import java.io.File;

import cis555.searchengine.utils.DocIDContentInfo;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class ContentDAO {

    private static EntityStore store;
    private static PrimaryIndex<String, DocIDContentInfo> contentIndex;

    /**
     * Initialize all the static variables
     *
     * @param dbPath
     *            The file path of the db
     */
    public static void setup(String dbPath) {
        // Create the directory in which this store will live.
        System.out.println("Setting up ContentDB.");
        File dir = new File(dbPath, "ContentDB");
        if (dir.mkdirs()) {
            System.out.println("Created PagerankDB directory.");
        }

        EnvironmentConfig envConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();
        envConfig.setAllowCreate(true);
        storeConfig.setAllowCreate(true);

        Environment env = new Environment(dir, envConfig);
        store = new EntityStore(env, "contentIndexStore", storeConfig);
        contentIndex = store.getPrimaryIndex(String.class,
                DocIDContentInfo.class);

        ShutdownHook hook = new ShutdownHook(env, store);
        Runtime.getRuntime().addShutdownHook(hook);

    }

    /**
     * Store the given DocIDContentInfo in the database.
     * 
     * @param docIDContent
     */
    public static void putContent(DocIDContentInfo docIDContent) {
        contentIndex.put(docIDContent);
    }

    /**
     * Store the given pair of docID, content in the database.
     * 
     * @param docID
     * @param content
     */
    public static void putPagerank(String docID, String content) {
        contentIndex.put(new DocIDContentInfo(docID, content));
    }

    /**
     * Retrieve the content from the database given its docID.
     * 
     * @param docID
     *            The primary key for the contentIndex.
     * @return content
     */
    public static String getContent(String docID) {
        DocIDContentInfo docContentInfo = contentIndex.get(docID);
        if (docContentInfo == null)
            return "";
        return docContentInfo.getContent();
    }

    /**
     * Retrieve a DocIDContentInfo from the database given its docID.
     * 
     * @param docID
     *            The primary key for the contentIndex.
     * @return DocIDContentInfo instance.
     */
    public static DocIDContentInfo getContentInfo(String docID) {
        return contentIndex.get(docID);
    }

}

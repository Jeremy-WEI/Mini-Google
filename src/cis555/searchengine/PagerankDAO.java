package cis555.searchengine;

import java.io.File;

import cis555.utils.DocIdPagerankInfo;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class PagerankDAO {
    private static EntityStore store;
    private static PrimaryIndex<String, DocIdPagerankInfo> pagerankIndex;

    /**
     * Initialize all the static variables
     *
     * @param dbPath
     *            The file path of the db
     */
    public static void setup(String dbPath) {
        // Create the directory in which this store will live.
        System.out.println("Setting up PagerankDB.");
        File dir = new File(dbPath, "PagerankDB");
        if (dir.mkdirs()) {
            System.out.println("Created PagerankDB directory.");
        }

        EnvironmentConfig envConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();
        envConfig.setAllowCreate(true);
        storeConfig.setAllowCreate(true);

        Environment env = new Environment(dir, envConfig);
        store = new EntityStore(env, "pagerankIndexStore", storeConfig);
        pagerankIndex = store.getPrimaryIndex(String.class,
                DocIdPagerankInfo.class);

        ShutdownHook hook = new ShutdownHook(env, store);
        Runtime.getRuntime().addShutdownHook(hook);

    }

    /**
     * Store the given DocIdPagerankInfo in the database.
     * 
     * @param pagerankInfo
     */
    public static void putUrlInfo(DocIdPagerankInfo pagerankinfo) {
        pagerankIndex.put(pagerankinfo);
    }

    /**
     * Store the given pair of docID and pagerank in the database.
     * 
     * @param docID
     * @param pagerank
     */
    public static void putPagerank(String docID, Double pagerank) {
        pagerankIndex.put(new DocIdPagerankInfo(docID, pagerank));
    }

    /**
     * Retrieve a pagerankInfo from the database given its docID.
     * 
     * @param docID
     *            The primary key for the pagerankIndex.
     * @return DocIdPagerankInfo instance.
     */
    public static DocIdPagerankInfo getPagerank(String docID) {
        return pagerankIndex.get(docID);
    }

    /**
     * Retrieve a pagerankValue from the database given its docID.
     * 
     * @param docID
     *            The primary key for the pagerankIndex.
     * @return pagerankValue.
     */
    public static double getPagerankValue(String docID) {
        DocIdPagerankInfo info = pagerankIndex.get(docID);
        if (info == null)
            return 0;
        return info.getPagerank();
    }

}

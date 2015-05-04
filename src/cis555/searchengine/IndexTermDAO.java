package cis555.searchengine;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import cis555.searchengine.utils.DocHitEntity;
import cis555.searchengine.utils.IndexTerm;
import cis555.utils.DocIdUrlInfo;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;

public class IndexTermDAO {
    private static EntityStore store;
    private static PrimaryIndex<Long, DocHitEntity> docHitEntityById;
    private static SecondaryIndex<String, Long, DocHitEntity> docHitEntityByWord;
    private static PrimaryIndex<String, IndexTerm> termIndex;

    /**
     * Initialize all the static variables
     *
     * @param dbPath
     *            The file path of the db
     */
    public static void setup(String dbPath) {
        // Create the directory in which this store will live.
        System.out.println("Setting up IndexTermDB.");
        File dir = new File(dbPath, "IndexTermDB");
        if (dir.mkdirs()) {
            System.out.println("Created IndexTermDB directory.");
        }

        EnvironmentConfig envConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();
        envConfig.setAllowCreate(true);
        storeConfig.setAllowCreate(true);

        Environment env = new Environment(dir, envConfig);
        store = new EntityStore(env, "IndexTermStore", storeConfig);

        termIndex = store.getPrimaryIndex(String.class, IndexTerm.class);
        docHitEntityById = store
                .getPrimaryIndex(Long.class, DocHitEntity.class);
        docHitEntityByWord = store.getSecondaryIndex(docHitEntityById,
                String.class, "word");

        ShutdownHook hook = new ShutdownHook(env, store);
        Runtime.getRuntime().addShutdownHook(hook);

    }

    /**
     * Store the given DocHitEntity in the database.
     * 
     * @param DocHitEntity
     */
    public static void putDocHitEntity(DocHitEntity docHitEntity) {
        docHitEntityById.put(docHitEntity);
    }

    /**
     * Store the given word and line as DocHitEntity in the database.
     * 
     * @param word
     * @param line
     */
    public static void putDocHitEntity(String word, String line, double avgWord) {
        docHitEntityById.put(new DocHitEntity(word, line, avgWord));
    }

    /**
     * Retrieve a DocHitEntity from the database given its key.
     * 
     * @param id
     *            The primary key for the DocHitEntity.
     * @return The DocHitEntity instance.
     */
    public static DocHitEntity getDocHitEntity(long id) {
        return docHitEntityById.get(id);
    }

    /**
     * Given a word return a Set<DocHit>; This function would automatically do a
     * checking: if the URL of docId is not saved in database, then it will not
     * be returned /IndexTermDAO.java
     * 
     * TODO: query word process should be done explicitly outside this function.
     */
    public static Set<DocHitEntity> getDocHitEntities(String word) {
        EntityCursor<DocHitEntity> cursor = docHitEntityByWord.entities(word,
                true, word, true);
        Set<DocHitEntity> set = new HashSet<DocHitEntity>();
        for (DocHitEntity docHit : cursor) {
            if (getUrl(docHit.getDocID()) != null) {
                set.add(docHit);
            }
        }
        cursor.close();
        return set;
    }

    /**
     * Returns cursor that iterates through the DocHitEntities.
     * 
     * @return A DocHitEntity cursor.
     */
    public static EntityCursor<Long> getDocHitEntityCursor() {
        CursorConfig cursorConfig = new CursorConfig();
        cursorConfig.setReadUncommitted(true);
        return docHitEntityById.keys(null, cursorConfig);
    }

    /**
     * Removes the DocHitEntity instance with the specified host.
     * 
     * @param id
     */
    public static void deleteDocHitEntity(long id) {
        docHitEntityById.delete(id);
    }

    public static String getUrl(String docID) {
        DocIdUrlInfo docIdUrlInfo = UrlIndexDAO.getUrlInfoIndex().get(docID);
        if (docIdUrlInfo == null)
            return null;
        return docIdUrlInfo.getURL();
    }

    /**
     * Store the given indexTerm in the database.
     * 
     * @param indexTerm
     */
    public static void putIndexTerm(IndexTerm IndexTerm) {
        termIndex.put(IndexTerm);
    }

    /**
     * Store the given word as an indexTerm in the database.
     * 
     * @param word
     */
    public static void putIndexTerm(String word) {
        termIndex.put(new IndexTerm(word));
    }

    /**
     * Store the given word and value as an indexTerm in the database.
     * 
     * @param word
     * @param value
     */
    public static void putIndexTerm(String word, double value) {
        termIndex.put(new IndexTerm(word, value));
    }

    /**
     * Retrieve the idf-value from the database given the word.
     * 
     * @param word
     *            The primary key for the indexTerm.
     * @return The idf-value.
     */
    public static double getIdfValue(String word) {
        IndexTerm indexTerm = termIndex.get(word);
        if (indexTerm == null)
            return 0;
        return termIndex.get(word).getIdfValue();
    }

    /**
     * Returns cursor that iterates through the indexTerm.
     * 
     * @return An indexTerm cursor.
     */
    public static EntityCursor<String> getIndexTermCursor() {
        CursorConfig cursorConfig = new CursorConfig();
        cursorConfig.setReadUncommitted(true);
        return termIndex.keys(null, cursorConfig);
    }
    
    
    public static TreeSet<String> getIndexTerms() {
		EntityCursor<String> cursor = null;
		TreeSet<String> set = new TreeSet<String>();

		try {
			cursor = IndexTermDAO.getIndexTermCursor();
			
			for (String word = cursor.first(); word != null; word = cursor.next()) {
				set.add(word);
			}
		} finally {
			cursor.close();
		}
		return set;
    }

    /**
     * Removes the indexTerm instance with the word.
     * 
     * @param word
     */
    public static void deleteIndexTerm(String word) {
        termIndex.delete(word);
    }

}

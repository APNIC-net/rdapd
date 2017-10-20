package net.apnic.whowas.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import net.apnic.whowas.history.ObjectClass;
import net.apnic.whowas.history.ObjectKey;
import net.apnic.whowas.history.ObjectSearchKey;
import net.apnic.whowas.history.Revision;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class WildCardSearchIndex
    implements SearchIndex
{
    private Directory directory = null;
    private final IndexExtractor<String> extractor;
    private final String indexAttribute;
    private final ObjectClass indexClass;
    private IndexWriter indexWriter = null;

    public WildCardSearchIndex(ObjectClass indexClass, String indexAttribute,
                               IndexExtractor<String> extractor)
    {
        this.extractor = extractor;
        this.indexAttribute = indexAttribute;
        this.indexClass = indexClass;
        setupIndex();
    }

    @Override
    public void commit()
    {
        try
        {
            indexWriter.commit();
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getIndexAttribute()
    {
        return indexAttribute;
    }

    @Override
    public ObjectClass getIndexClass()
    {
        return indexClass;
    }

    @Override
    public Stream<ObjectKey> getObjectsForKey(ObjectSearchKey objectSearchKey,
                                              int limit)
    {
        try
        {
            DirectoryReader reader = DirectoryReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(new WildcardQuery(new Term(getIndexAttribute(), objectSearchKey.getObjectName())), limit);

            ObjectKey[] keys = new ObjectKey[docs.scoreDocs.length];
            for(int i = 0; i < docs.scoreDocs.length; ++i)
            {
                keys[i] = new ObjectKey(getIndexClass(),
                    searcher.doc(docs.scoreDocs[i].doc).get("handle"));
            }
            return Arrays.stream(keys);
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void putMapping(Revision revision, ObjectKey objectKey)
    {
        extractor.extract(revision, objectKey)
            .forEach(key ->
            {
                Document doc = new Document();
                doc.add(new StringField(getIndexAttribute(), key, Field.Store.YES));
                doc.add(new StoredField("handle", objectKey.getObjectName()));

                try
                {
                    indexWriter.addDocument(doc);
                }
                catch(Exception ex)
                {
                    throw new RuntimeException(ex);
                }
            });
    }

    private void setupIndex()
    {
        directory = new RAMDirectory();
        IndexWriterConfig iwConfig = new IndexWriterConfig(new KeywordAnalyzer());
        iwConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        try
        {
            indexWriter = new IndexWriter(directory, iwConfig);
        }
        catch(IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}

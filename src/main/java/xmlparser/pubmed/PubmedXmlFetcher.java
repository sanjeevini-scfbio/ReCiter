package xmlparser.pubmed;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import xmlparser.AbstractXmlFetcher;
import xmlparser.pubmed.model.PubmedArticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Fetches XML articles from PubMed and writes to a file. One can specify the location
 * where the fetched XML files will go, otherwise a default location will be used based
 * on the configuration setting.
 * 
 * @author jil3004
 *
 */
public class PubmedXmlFetcher extends AbstractXmlFetcher {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(AbstractXmlFetcher.class);
	private static final String DEFAULT_LOCATION = "src/main/java/resources/data/xml";

	/**
	 * 
	 * @param lastName
	 * @param firstInitial
	 * @param cwid
	 * @return
	 */
	public List<PubmedArticle> getPubmedArticle(String lastName, String firstInitial, String cwid) {
		List<PubmedArticle> pubmedArticleList = new ArrayList<PubmedArticle>();

		// The articles are retrieved when this directory exists. Fetch the articles if they do not exist.
		//		File dir = new File(getDirectory() + cwid);
		//		if (!dir.exists()) {
		//			fetch(lastName, firstInitial, cwid);
		//		}

		if (isPerformRetrievePublication()) {
			fetch(lastName, firstInitial, cwid);
		}

		// Parse the xml files to return a list of PubmedArticles.
		File[] xmlFiles = new File(getDirectory() + cwid).listFiles();
		for (File xmlFile : xmlFiles) {
			PubmedEFetchHandler pubmedEFetchHandler = new PubmedEFetchHandler();
			try {
				SAXParserFactory.newInstance()
				.newSAXParser()
				.parse(xmlFile.getPath(), pubmedEFetchHandler);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pubmedArticleList.addAll(pubmedEFetchHandler.getPubmedArticles());
		}
		return pubmedArticleList;
	}

	/**
	 * Fetch all the publications for this query "lastname firstInitial[au]" in PubMed and store it on disk.
	 * @param lastName last name of the author.
	 * @param firstInitial first initial of the author.
	 * @param cwid cwid of the author.
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public void fetch(String lastName, String firstName, String cwid) {

		File dir = new File(getDirectory() + cwid);
		int numPubMedArticles = -1;
		// Fetch only if directory doesn't exist.
		if (!dir.exists()) {
			String firstInitial = firstName.substring(0, 1);

			// Get the count (number of publications for this query).
			PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery();
			lastName = lastName.replaceAll(" ", "%20");
			pubmedXmlQuery.setTerm(lastName + "%20" + firstInitial + "[au]");

			// set retmax = 1 so that query can be executed fast.
			pubmedXmlQuery.setRetMax(1);

			String eSearchUrl = pubmedXmlQuery.buildESearchQuery();
			PubmedESearchHandler xmlHandler = PubmedESearchHandler.executeESearchQuery(eSearchUrl);
			numPubMedArticles = xmlHandler.getCount();

			slf4jLogger.info("Number of articles need to be retrieved for : " + cwid + " is "+ numPubMedArticles);

			// Retrieve the publications 10,000 records at one time and store to disk.
			int retMax = 10000;
			pubmedXmlQuery.setRetMax(retMax);
			int currentRetStart = 0;

			// Number of partitions that we need to finish retrieving all XML.
			int numSteps = (int) Math.ceil((double)numPubMedArticles / retMax); 

			// Use the retstart value to iteratively fetch all XMLs.
			for (int i = 0; i < numSteps; i++) {
				// Get webenv value.
				pubmedXmlQuery.setRetStart(currentRetStart);
				eSearchUrl = pubmedXmlQuery.buildESearchQuery();
				pubmedXmlQuery.setWevEnv(PubmedESearchHandler.executeESearchQuery(eSearchUrl).getWebEnv());

				// Use the webenv value to retrieve xml.
				String eFetchUrl = pubmedXmlQuery.buildEFetchQuery();

				// Save the xml file to directory data/xml/cwid
				saveXml(eFetchUrl, cwid, cwid + "_" + i);

				// Update the retstart value.
				currentRetStart += pubmedXmlQuery.getRetMax();
				pubmedXmlQuery.setRetStart(currentRetStart);
			}
		}
	}

	/**
	 * 
	 * @param filePath
	 * @param expectedResult
	 * @return
	 */
	public boolean isNumberOfArticleMatch(String filePath, int expectedResult) {
		List<PubmedArticle> pubmedArticleList = new ArrayList<PubmedArticle>();
		
		// Parse the XML files in this location to return a list of PubmedArticles.
		File[] xmlFiles = new File(filePath).listFiles();
		for (File xmlFile : xmlFiles) {
			PubmedEFetchHandler pubmedEFetchHandler = new PubmedEFetchHandler();
			try {
				SAXParserFactory.newInstance()
				.newSAXParser()
				.parse(xmlFile.getPath(), pubmedEFetchHandler);
			} catch (Exception e) {
				slf4jLogger.error(e.getMessage());
			}
			pubmedArticleList.addAll(pubmedEFetchHandler.getPubmedArticles());
		}

		return pubmedArticleList.size() == expectedResult;
	}

	/**
	 * <p>
	 * Performs a check on a PubMed retrieved XML file for {@code <ERROR>Unable to obtain query #1</ERROR>} on
	 * {@code line 3}. 
	 * </p>
	 * 
	 * <p>
	 * This function might be useful if you need to perform a check on the XML files for this error
	 * message, and perform a re-retrieval if necessary.
	 * </p>
	 * 
	 * @param filePath file location of the PubMed XML file.
	 * @return true if XML file contains the {@code <ERROR>Unable to obtain query #1</ERROR>}, false otherwise.
	 */
	public boolean isUnableToObtainQueryError(String filePath) {
		int lineNumber = 3;
		String lineContent = null;
		String unableToObtainQueryMessage = "<ERROR>Unable to obtain query #1</ERROR>";
		try {
			lineContent = Files.lines(Paths.get(filePath))
					.skip(lineNumber)
					.findFirst()
					.get()
					.trim();
		} catch (IOException e) {
			slf4jLogger.error(e.getMessage());
		}
		return unableToObtainQueryMessage.equals(lineContent);
	}

	public PubmedXmlFetcher() {
		super(DEFAULT_LOCATION);
	}

	public PubmedXmlFetcher(String directory) {
		super(directory);
	}

}
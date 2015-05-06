package main.xml.pubmed.model;

public class MedlineCitationJournalIssue {

	private enum CitedMedium {
		INTERNET,
		PRINT
	}
	
	private CitedMedium citedMedium;
	private String volume;
	private String issue;
	private MedlineCitationDate pubDate;
	
	public MedlineCitationDate getPubDate() {
		return pubDate;
	}
	public void setPubDate(MedlineCitationDate pubDate) {
		this.pubDate = pubDate;
	}
		
}
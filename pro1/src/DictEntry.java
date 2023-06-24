import java.util.*;

class DictEntry {
    private String term;
    private int termFrequency;
    private List<posting> positions;

    public DictEntry(String term)
    {
        this.term = term;
        this.termFrequency = 0;
        this.positions = new ArrayList<>();


    }

   public void incrementTermFrequency() {
        this.termFrequency++;
    }

    public List<posting> getPositions() {
        return positions;
    }




}

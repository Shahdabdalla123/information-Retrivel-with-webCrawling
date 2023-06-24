class posting {
    private int docId;
    private int position;

    public posting(int docId, int position) {
        this.docId = docId;
        this.position = position;
    }

    public int getDocId() {
        return docId;
    }

    public int getPosition() {
        return position;
    }
}
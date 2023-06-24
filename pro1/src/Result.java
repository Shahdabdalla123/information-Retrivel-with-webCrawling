class Result {
    private String fileName;
    private Double score;

    public Result(String fileName, Double score) {
        this.fileName = fileName;
        this.score = score;
    }

    public String getFileName() {
        return fileName;
    }

    public Double getScore() {
        return score;
    }
}
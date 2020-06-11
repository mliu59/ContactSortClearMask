package core;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;

public class WordLib {

    private int keywordCount = 0;
    public int getKeywordCount() {
        return keywordCount;
    }

    public Map<String, strPair> jobTitleMap = new HashMap<>();
    public Map<String, strPair> companyNameMap = new HashMap<>();
    public Map<String, strPair> membershipNoteMap = new HashMap<>();
    public Map<String, strPair> emailAddressMap = new HashMap<>();
    public Map<String, strPair> blanketSearchMap = new HashMap<>();

    String jobTitleToken = "Job Title";
    String companyNameToken = "Company Name";
    String membershipNotesToken = "Notes";
    String emailAddressToken = "Email";
    String allToken = "All";
    String blanketSearchToken = "Blanket Check";

    categoryCounter counter = new categoryCounter();

    public WordLib(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        while (line != null) {
            insertKeywordToMaps(line);
            line = reader.readLine();
        }
    }

    private void insertKeywordToMaps(String keywordLine) {
        String[] fields = keywordLine.split(",", 4);
        for (int i = 0; i < 4; i++) {
            if (fields[i] == null) {
                fields[i] = "";
            }
        }
        strPair catg = new strPair(fields[2], fields[3]);
        if (fields[0].equals(jobTitleToken)) {
            jobTitleMap.put(fields[1], catg);
            keywordCount++;
        } else if (fields[0].equals(companyNameToken)) {
            companyNameMap.put(fields[1], catg);
            keywordCount++;
        } else if (fields[0].equals(membershipNotesToken)) {
            membershipNoteMap.put(fields[1], catg);
            keywordCount++;
        } else if (fields[0].equals(emailAddressToken)) {
            emailAddressMap.put(fields[1], catg);
            keywordCount++;
        } else if (fields[0].equals(allToken)) {
            jobTitleMap.put(fields[1], catg);
            companyNameMap.put(fields[1], catg);
            membershipNoteMap.put(fields[1], catg);
            emailAddressMap.put(fields[1], catg);
            keywordCount++;
        } else if (fields[0].equals(blanketSearchToken)) {
            blanketSearchMap.put(fields[1], catg);
            keywordCount++;
        }
    }

    static class categoryCounter {

        private Map<String, Integer> fieldIndustryCounter = new HashMap<>();
        private Map<String, Integer> personTypeCounter = new HashMap<>();
        private int noCategoryCount = 0;
        private int noPersonCount = 0;

        public void noCategory() {
            noCategoryCount++;
        }
        public void noPerson() {
            noPersonCount++;
        }

        public void insertCategory(String token) {
            if (fieldIndustryCounter.containsKey(token)) {
                int count = fieldIndustryCounter.get(token);
                fieldIndustryCounter.replace(token, count + 1);
            } else {
                fieldIndustryCounter.put(token, 1);
            }
        }

        public void insertPerson(String token) {
            if (personTypeCounter.containsKey(token)) {
                int count = personTypeCounter.get(token);
                personTypeCounter.replace(token, count + 1);
            } else {
                personTypeCounter.put(token, 1);
            }
        }

        public String toString(int processed) {
            return "\nField/Industry Counts\n" +
                    mapToString(fieldIndustryCounter, processed) +
                    reportNoCategory(processed) +
                    "\nPerson Type Counts\n" +
                    mapToString(personTypeCounter, processed) +
                    reportNoPerson(processed);
        }

        private String reportNoCategory(int processed) {
            return "No Desig. Field/Industry: " + noCategoryCount + ", " + percentage(noCategoryCount, processed) + "\n";
        }

        private String reportNoPerson(int processed) {
            return "No Desig. Person Type: " + noPersonCount + ", " + percentage(noPersonCount, processed) + "\n";
        }

        public String percentage(int num, int dem) {
            DecimalFormat df = new DecimalFormat("##.##");
            return df.format((double) num / dem * 100) + "%";
        }

        private String mapToString(Map<String, Integer> map, int processed) {
            StringBuilder output = new StringBuilder();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                output.append(entry.getKey());
                output.append(": ");
                output.append(entry.getValue());
                output.append(", ");
                output.append(percentage(entry.getValue(), processed));
                output.append(";\n");

            }
            return output.toString();
        }

    }
}

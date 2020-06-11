package core;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

public class SearchAlg {

    static class Indices {

        private int totalNumFields;

        private int companyNameIndex;
        private int jobTitleIndex;
        private int membershipNoteIndex;
        private int autoGenIndex;
        private int fieldIndustryIndex;
        private int personTypeIndex;
        private int emailAddressIndex;
        private int contactOwnerIndex;

        public Indices(String header) {
            updateIndex(header);
            findTotalNumFields(header);
        }

        public void updateIndex(String header) {
            String companyNameToken = "Company Name";
            String emailAddressToken = "Email";
            String jobTitleToken = "Job Title";
            String notesToken = "Membership Notes";
            String fieldToken = "Field/Industry";
            String personTypeToken = "Person Type";
            String autoGenToken = "AutoGen";
            String contactOwnerToken = "Contact owner";

            companyNameIndex = getColumn(header, companyNameToken);
            emailAddressIndex = getColumn(header, emailAddressToken);
            jobTitleIndex = getColumn(header, jobTitleToken);
            membershipNoteIndex = getColumn(header, notesToken);
            fieldIndustryIndex = getColumn(header, fieldToken);
            personTypeIndex = getColumn(header, personTypeToken);
            autoGenIndex = getColumn(header, autoGenToken);
            contactOwnerIndex = getColumn(header, contactOwnerToken);
        }

        private void findTotalNumFields(String header) {
            String[] colHead = header.split(",");
            totalNumFields = colHead.length;
        }

        private int getColumn(String header, String search) throws IllegalArgumentException {
            String[] colHead = header.split(",");
            for (int i = 0; i < colHead.length; i++) {
                if (colHead[i].equals(search)) {
                    return i;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    public static void main(String[] args) {
        int numOfInputEntries = 0;
        int numOfProcessedEntries = 0;
        int numOfPreProcessedEntries = 0;

        File contactInputFile = getFile();
        File keywordFile = getFile();

        BufferedReader contactReader;
        BufferedReader keywordLibraryReader;

        String outputName = ".\\OutputContacts.csv";
        String outputReportName = ".\\OutputReport.txt";

        try {
            contactReader = initContactReader(contactInputFile);
            keywordLibraryReader = initContactReader(keywordFile);

            FileWriter output = new FileWriter(new File(outputName));

            String header = contactReader.readLine();
            output.write(header + "\n");
            Indices head = new Indices(header);
            WordLib lib = new WordLib(keywordLibraryReader);

            String line = contactReader.readLine();

            while (line != null) {

                Entry n = new Entry(line, head.totalNumFields, head.companyNameIndex, head.jobTitleIndex, head.membershipNoteIndex, head.autoGenIndex, head.fieldIndustryIndex, head.personTypeIndex, head.emailAddressIndex, head.contactOwnerIndex);
                numOfInputEntries++;
                String email = "";
                String note = "";
                String company = "";
                String job = "";
                if (!n.isProcessed()) {
                    ExtractedEntry extractedEntry = new ExtractedEntry(n, email, note, company, job).invoke();
                    email = extractedEntry.getEmail();
                    note = extractedEntry.getNote();
                    company = extractedEntry.getCompany();
                    job = extractedEntry.getJob();
                    ArrayList<String>[] original = arrayListDeepCopy(n.genFields);
                    trimEmailAndSearch(lib, n, email);
                    search(n, note, lib.membershipNoteMap, lib);
                    search(n, company, lib.companyNameMap, lib);
                    search(n, job, lib.jobTitleMap, lib);

                    testMinimalContact(n);

                    if (Arrays.equals(original, n.genFields)) {
                        search(n, note, lib.blanketSearchMap, lib);
                        search(n, company, lib.blanketSearchMap, lib);
                        search(n, job, lib.blanketSearchMap, lib);
                    }

                    if (!Arrays.equals(original, n.genFields)) {
                        n.updateAutogen();
                    }

                    if (!n.genFields[n.autoGenIndex].isEmpty()) {
                        numOfProcessedEntries++;
                    }
                } else {
                    if (n.fieldIndustryFilled()) {
                        n.genFields[n.fieldIndustryIndex] = n.delineateField(n.genFields[n.fieldIndustryIndex]);
                        for (String a : n.genFields[n.fieldIndustryIndex]) lib.counter.insertCategory(a);
                    }
                    if (n.personTypeFilled()) {
                        n.genFields[n.personTypeIndex] = n.delineateField(n.genFields[n.personTypeIndex]);
                        for (String a : n.genFields[n.personTypeIndex]) lib.counter.insertPerson(a);
                    }
                    numOfPreProcessedEntries++;
                }
                checkAndUpdateEmptyFieldNum(n, lib);

                String UpdatedEntry = n.toString();
                output.write(UpdatedEntry + "\n");
                line = contactReader.readLine();
            }
            contactReader.close();
            keywordLibraryReader.close();
            output.close();

            genReport(lib.getKeywordCount(), numOfInputEntries, numOfProcessedEntries, numOfPreProcessedEntries, outputReportName, lib);

        } catch (IOException er) {
            er.printStackTrace();
        }
    }

    private static void checkAndUpdateEmptyFieldNum(Entry n, WordLib lib) {
        if (!n.fieldIndustryFilled()) {
            lib.counter.noCategory();
        }
        if (!n.personTypeFilled()) {
            lib.counter.noPerson();
        }
    }

    private static void trimEmailAndSearch(WordLib lib, Entry n, String email) {
        String emailDomain;
        if (email.contains("@")) {
            emailDomain = email.substring(email.indexOf('@'));
        } else {
            emailDomain = email;
        }
        search(n, emailDomain, lib.emailAddressMap, lib);
    }

    private static void search(Entry n, String str, Map<String, strPair> jobTitleMap, WordLib lib) {
        for (Map.Entry<String, strPair> keyword : jobTitleMap.entrySet()) {
            searchAndUpdate(n, str, keyword, lib);
        }
    }

    private static void searchAndUpdate(Entry n, String toSearch, Map.Entry<String, strPair> keyword, WordLib lib) {
        String field = keyword.getValue().getFirst();
        String person = keyword.getValue().getSecond();
        String key = keyword.getKey().toLowerCase();
        if (key.charAt(0) == '#') {
            key = key.substring(1);
            if (toSearch.equals(key)) {
                updateFields(n, field, person, lib);
            }
        } else if (toSearch.toLowerCase().contains(key)) {
            updateFields(n, field, person, lib);
        }
    }

    private static void updateFields(Entry n, String field, String person, WordLib lib) {
        if (!field.equals("")) {
            if (!n.genFields[n.fieldIndustryIndex].contains(field)) {
                n.genFields[n.fieldIndustryIndex].add(field);
                lib.counter.insertCategory(field);
            }
        }
        if (!person.equals("")) {
            if (!n.genFields[n.personTypeIndex].contains(person)) {
                n.genFields[n.personTypeIndex].add(person);
                lib.counter.insertPerson(person);
            }
        }
    }

    private static File getFile() {
        File contactInputFile = null;
        JFileChooser jfc1 = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        int returnValue = jfc1.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            contactInputFile = jfc1.getSelectedFile();
        }
        return contactInputFile;
    }

    private static BufferedReader initContactReader(File contactInputFile) throws FileNotFoundException {
        BufferedReader contactReader;
        assert contactInputFile != null;
        contactReader = new BufferedReader(new FileReader(contactInputFile));
        return contactReader;
    }

    private static void testMinimalContact(Entry n) {
        if (n.minimalContact()) {
            n.updateAutogen();
        }
    }

    private static void genReport(int kwc, int input, int processed, int prep, String reportName, WordLib lib) throws IOException {
        FileWriter report = new FileWriter(new File(reportName));
        report.write("Number of Keywords Imported: " + kwc + "\n");
        report.write("Number of Contact Entries Imported: " + input + "\n");
        report.write("Number of Contact Entries Processed: " + processed + "\n");
        report.write("Number of Pre-processed Contact Entries: " + prep + "\n");
        report.write("Percent of Contacts Updated: " + lib.counter.percentage(processed, input) + "\n");
        report.write("Percent of Contacts Pre-Filled: " + lib.counter.percentage(prep, input) + "\n");
        report.write(lib.counter.toString(input));
        report.close();
    }

    private static ArrayList<String>[] arrayListDeepCopy(ArrayList<String>[] genFields) {
        ArrayList<String>[] original = new ArrayList[genFields.length];
        for (int i = 0; i < genFields.length; i++) {
            original[i] = (ArrayList<String>) genFields[i].clone();
        }
        return original;
    }

    private static class ExtractedEntry {
        private Entry n;
        private String email;
        private String note;
        private String company;
        private String job;

        public ExtractedEntry(Entry n, String email, String note, String company, String job) {
            this.n = n;
            this.email = email;
            this.note = note;
            this.company = company;
            this.job = job;
        }

        private static String filterHyphen(String s) {
            return s.replaceAll("-", " ");
        }
        public String getEmail() {
            return email;
        }
        public String getNote() {
            return note;
        }
        public String getCompany() {
            return company;
        }
        public String getJob() {
            return job;
        }

        public ExtractedEntry invoke() {
            if (!n.genFields[n.emailAddressIndex].isEmpty()) {
                email = n.genFields[n.emailAddressIndex].get(0).toLowerCase();
                email = filterHyphen(email);
            }
            if (!n.genFields[n.membershipNoteIndex].isEmpty()) {
                note = n.genFields[n.membershipNoteIndex].get(0).toLowerCase();
                note = filterHyphen(note);
            }
            if (!n.genFields[n.companyNameIndex].isEmpty()) {
                company = n.genFields[n.companyNameIndex].get(0).toLowerCase();
                company = filterHyphen(company);
            }
            if (!n.genFields[n.jobTitleIndex].isEmpty()) {
                job = n.genFields[n.jobTitleIndex].get(0).toLowerCase();
            }
            return this;
        }
    }
}





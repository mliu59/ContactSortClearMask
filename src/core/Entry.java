package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.StringBuilder;

public class Entry {

    static int numFields;
    public ArrayList<String>[] genFields;

    int companyNameIndex;
    int jobTitleIndex;
    int membershipNoteIndex;
    int autoGenIndex;
    int fieldIndustryIndex;
    int personTypeIndex;
    int emailAddressIndex;
    int contactOwnerIndex;


    public Entry(String line, int numFields, int companyNameIndex, int jobTitleIndex, int membershipNoteIndex, int autoGenIndex, int fieldIndustryIndex, int personTypeIndex, int emailAddressIndex, int contactOwnerIndex) {
        Entry.numFields = numFields;
        genFields = new ArrayList[numFields];
        populateGenFields(line);
        this.companyNameIndex = companyNameIndex;
        this.jobTitleIndex = jobTitleIndex;
        this.membershipNoteIndex = membershipNoteIndex;
        this.autoGenIndex = autoGenIndex;
        this.fieldIndustryIndex = fieldIndustryIndex;
        this.personTypeIndex = personTypeIndex;
        this.emailAddressIndex = emailAddressIndex;
        this.contactOwnerIndex = contactOwnerIndex;
    }

    private void populateGenFields(String input) {
        String[] fullEntry = csvSplit(input, numFields);
        for (int i = 0; i < numFields; i++) {
            genFields[i] = new ArrayList<>();
            if (!fullEntry[i].equals("")) {
                genFields[i].add(fullEntry[i]);
            }
        }
    }

    public ArrayList<String> delineateField(ArrayList<String> list) throws IllegalArgumentException{
        if (list.isEmpty()) {
            throw new IllegalArgumentException();
        }
        String[] array = list.get(0).split("; ");
        return new ArrayList<>(Arrays.asList(array));
    }

    private String[] csvSplit(String org, int num) {
        String[] array = new String[num];
        if (org.indexOf('"') == -1) {
            array = org.split(",", num);
        } else {
            String mod = org;
            int count = 0;
            while (count < num) {
                if (count == num - 1) {
                    array[count++] = mod;
                } else if (mod.charAt(0) == '"') {
                    int end = mod.indexOf("\",");
                    array[count++] = mod.substring(0, end + 1);
                    mod = mod.substring(end + 2);
                } else {
                    int end = mod.indexOf(',');
                    array[count++] = mod.substring(0, end);
                    mod = mod.substring(end + 1);
                }
            }
        }
        for (int i = 0; i < num; i++) {
            if (array[i].length() > 1) {
                if (array[i].charAt(0) == '"' && array[i].charAt(array[i].length() - 1) == '"') {
                    array[i] = array[i].substring(1, array[i].length() - 1);
                }
            }
        }
        return array;
    }

    public boolean isProcessed() {
        return autoGen_ed() || fieldIndustryFilled() || personTypeFilled();
    }
    public boolean autoGen_ed() {
        return genFields[autoGenIndex].contains("Yes");
    }
    public boolean fieldIndustryFilled() {
        return !genFields[fieldIndustryIndex].isEmpty();
    }
    public boolean personTypeFilled() {
        return !genFields[personTypeIndex].isEmpty();
    }
    public boolean contactOwnerFilled() {
        return !genFields[contactOwnerIndex].isEmpty();
    }
    public boolean companyNameFilled() {
        return !genFields[companyNameIndex].isEmpty();
    }
    public boolean jobTitleFilled() {
        return !genFields[jobTitleIndex].isEmpty();
    }
    public boolean noteFilled() {
        return !genFields[membershipNoteIndex].isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < numFields - 1; i++) {
            String a = listToString(genFields[i]);
            if (!a.equals("")) {
                output.append("\"");
                output.append(a);
                output.append("\",");
            } else {
                output.append(",");
            }
        }
        String a = listToString(genFields[numFields - 1]);
        if (!a.equals("")) {
            output.append("\"");
            output.append(a);
            output.append("\"");
        }
        return output.toString();
    }

    private String listToString(List<String> list) {
        if (list.isEmpty()) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < list.size() - 1; i++) {
            str.append(list.get(i));
            str.append("; ");
        }
        str.append(list.get(list.size() - 1));
        return str.toString();
    }

    public boolean minimalContact() {
        return !fieldIndustryFilled() && !personTypeFilled() && !contactOwnerFilled() && !companyNameFilled() && !jobTitleFilled() && !noteFilled();
    }

    public void updateAutogen() {
        if (genFields[autoGenIndex].isEmpty()) {
            genFields[autoGenIndex].add("Yes");
        }
    }
}

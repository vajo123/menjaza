package util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class StickerGenerator {

    public static Set<Integer> generateDuplicates(int count) {
        Random random = new Random();
        Set<Integer> set = new HashSet<>();

        while (set.size() < count) {
            set.add(random.nextInt(99) + 1);
        }

        return set;
    }

    public static Set<Integer> generateMissing(Set<Integer> duplicates, int count) {
        Random random = new Random();
        Set<Integer> missing = new HashSet<>();

        while (missing.size() < count) {
            int num = random.nextInt(99) + 1;
            if (!duplicates.contains(num)) {
                missing.add(num);
            }
        }

        return missing;
    }
}
package mine.block.spotify;

import java.util.Arrays;

public class Lyrics {


    enum SyncType {
        LINE_SYNCED,
        WORD_SYNCED,
        UNSYNCED
    }

    public SyncType syncType;

    public static class Line {
        public long startTimeMs;
        public String words;
        public String[] syllables;
        public long endTimeMs;
    }

    public Line[] lines;

    public Line[] gatherLinesForTimestamp(Long timestamp, int surroundingLinesCount) {

        // TODO: Okay maybe this shouldn't be done here as we have on idea on where to start searching, so we need to restart every time
        int i;
        for (i = 0; i < lines.length; i++) {
            if(lines[i].startTimeMs > timestamp) {
              break;
            }
        }

        int start = Math.max(0, i - surroundingLinesCount);

        return Arrays.stream(lines).skip(start).limit(surroundingLinesCount * 2L + 1).toArray(Line[]::new);
    }
}

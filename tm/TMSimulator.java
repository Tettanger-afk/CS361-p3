package tm;

// This is the driver class for the Turing Machine Simulator application.
public class TMSimulator {

    public static void main(String[] args) {

        java.io.File cwd = new java.io.File(System.getProperty("user.dir"));
        java.io.File[] inputs;
        // defaults
        int defaultUnary = 1;
        boolean timing = false; // preserved for backward compatibility but unused when timing always printed

        // simple arg parsing: global flags then file names
        java.util.List<String> fileArgs = new java.util.ArrayList<>();
        for (String a : args) {
            if (a.startsWith("--unary=")) {
                try { defaultUnary = Integer.parseInt(a.substring("--unary=".length())); } catch (Exception ex) { System.err.println("Invalid --unary value: " + a); }
                continue;
            }
            if ("--timing".equals(a)) {
                timing = true;
                continue;
            }
            fileArgs.add(a);
        }
        if (fileArgs.size() > 0) {
            inputs = new java.io.File[fileArgs.size()];
            for (int i = 0; i < fileArgs.size(); i++) inputs[i] = new java.io.File(fileArgs.get(i));
        } else if (args.length == 0) {
            inputs = cwd.listFiles((d, name) -> name.endsWith(".txt") && !name.startsWith("."));
            if (inputs == null) inputs = new java.io.File[0];
        } else {
            // flags provided but no filenames -> search directory
            inputs = cwd.listFiles((d, name) -> name.endsWith(".txt") && !name.startsWith("."));
            if (inputs == null) inputs = new java.io.File[0];
        }

        if (inputs.length == 0) {
            System.err.println("No input files found. Provide .txt machine files as arguments or place them in the working directory.");
            return;
        }

        // Simple cache: file path -> (lastModified, template TM)
        final java.util.Map<String, CacheEntry> cache = new java.util.HashMap<>();

        for (java.io.File f : inputs) {
            try {
                long last = f.lastModified();
                CacheEntry entry = cache.get(f.getAbsolutePath());
                if (entry == null || entry.lastModified != last) {
                    entry = processFileBuildTemplate(f);
                    entry.lastModified = last;
                    cache.put(f.getAbsolutePath(), entry);
                }

                // create runnable instance from template
                TM tm = entry.tmTemplate.cloneTemplate();
                // build fast transition table for runtime using the parsed machine parameters
                if (entry.nStates > 0 && entry.symbolsPerState > 0) {
                    tm.buildTransitionTable(entry.nStates, entry.symbolsPerState);
                }
                // initialize tape from file input if provided, otherwise use unary default
                if (entry.initialInput != null) {
                    tm.initializeTape(entry.initialInput);
                } else {
                    tm.initializeUnaryInput(defaultUnary);
                }
                tm.setCurrentState(0);
                // per spec: run until the machine halts (no artificial step cap)
                long start = System.nanoTime();
                tm.run();
                long end = System.nanoTime();
                double elapsed = (end - start) / 1_000_000_000.0;

                // report
                System.out.println(f.getName());
                final int LARGE_THRESHOLD = 1000; // if visited length larger, print "very large"
                int visitedLen = tm.getVisitedLength();
                String content = tm.getVisitedContentString();
                if (visitedLen > LARGE_THRESHOLD) {
                    System.out.println("output: very large");
                } else {
                    System.out.println("output:");
                    System.out.println(content);
                }
                System.out.println("output length: " + visitedLen);
                System.out.println("sum of symbols: " + tm.getSumOfSymbols());
                // preserve a trailing blank line to match expected output files
                System.out.println();
                // always print elapsed timing (printed to stdout so it's visible in terminal)
                System.out.printf("elapsed (s): %.3f\n", elapsed);

            } catch (Exception e) {
                System.err.println("Error processing " + f + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static CacheEntry processFileBuildTemplate(java.io.File f) throws Exception {
        java.util.List<String> lines = java.nio.file.Files.readAllLines(f.toPath());
        java.util.List<String> trimmed = new java.util.ArrayList<>();
        for (String L : lines) {
            String t = L.trim();
            if (!t.isEmpty()) trimmed.add(t);
        }
        if (trimmed.size() < 2) throw new IllegalArgumentException("file too short: " + f.getName());

        int idx = 0;
        int nStates = Integer.parseInt(trimmed.get(idx++));
        int sCount = Integer.parseInt(trimmed.get(idx++)); // number of input symbols (1..sCount)

        TM tm = new TM();
        // create states 0..nStates-1
        for (int i = 0; i < nStates; i++) {
            TMState st = new TMState(i);
            if (i == nStates - 1) st.setHalting(true);
            tm.addState(st);
        }

        // transitions expected for states 0 .. nStates-2 and symbols 0 .. sCount (including blank 0)
        int symbolsPerState = sCount + 1;
        int expected = (nStates - 1) * symbolsPerState;
        if (trimmed.size() - idx < expected) throw new IllegalArgumentException("not enough transition lines in " + f.getName());

        for (int state = 0; state <= nStates - 2; state++) {
            for (int sym = 0; sym <= sCount; sym++) {
                String line = trimmed.get(idx++);
                String[] parts = line.split(",");
                if (parts.length < 3) throw new IllegalArgumentException("bad transition line: " + line);
                int next = Integer.parseInt(parts[0].trim());
                int write = Integer.parseInt(parts[1].trim());
                char dir = parts[2].trim().charAt(0);
                TMStateInterface st = tm.getState(state);
                st.addTransition(sym, next, write, dir);
            }
        }

        // set blank symbol to 0 by definition
        tm.setBlankSymbol(0);

        // parse input string if present. We must detect an explicit input line
        // (possibly blank) vs no input line at all. Use the original raw lines
        // to decide: find the raw index of the last consumed non-empty line,
        // then check if there is any raw line after that (even blank) -> explicit input.
        int[] initialInput;
        int rawConsumed = 0;
        int rawIndexOfLastConsumed = -1;
        for (int ri = 0; ri < lines.size(); ri++) {
            if (!lines.get(ri).trim().isEmpty()) {
                rawConsumed++;
                if (rawConsumed == idx) { rawIndexOfLastConsumed = ri; break; }
            }
        }
        if (rawIndexOfLastConsumed >= 0 && rawIndexOfLastConsumed + 1 < lines.size()) {
            // There is a raw line after the last non-empty transition line -> explicit input (may be blank)
            String inputLine = lines.get(rawIndexOfLastConsumed + 1).trim();
            java.util.List<Integer> digits = new java.util.ArrayList<>();
            for (char c : inputLine.toCharArray()) if (Character.isDigit(c)) digits.add(c - '0');
            initialInput = new int[digits.size()];
            for (int i = 0; i < digits.size(); i++) initialInput[i] = digits.get(i);
        } else {
            // no explicit input line -> heuristically treat unary machines (sCount==1)
            // as using the unary default, otherwise treat as explicit empty input.
            if (sCount == 1) initialInput = null; else initialInput = new int[0];
        }

        return new CacheEntry(0L, tm, initialInput, nStates, symbolsPerState);
    }

    private static class CacheEntry {
        long lastModified;
        TM tmTemplate;
        int[] initialInput;
        int nStates;
        int symbolsPerState;
        CacheEntry(long lastModified, TM tmTemplate, int[] initialInput, int nStates, int symbolsPerState) { this.lastModified = lastModified; this.tmTemplate = tmTemplate; this.initialInput = initialInput; this.nStates = nStates; this.symbolsPerState = symbolsPerState; }
    }

} // end of TMSimulator class

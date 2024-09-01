package id.my.rezahanif.Cache;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FileLocalCache {
    private Map<String, File> fileCache;

    public FileLocalCache() {
        fileCache = new HashMap<>();
    }

    public File getFile(String path) {
        File file = fileCache.get(path);

        if (file == null) {
            // Jika file tidak ada dalam cache, buat dan simpan dalam cache
            file = new File(path);
            fileCache.put(path, file);
        }

        // Mengembalikan File
        return file;
    }
}

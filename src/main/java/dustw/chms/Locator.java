package dustw.chms;

import com.mojang.logging.LogUtils;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.ModDirTransformerDiscoverer;
import net.minecraftforge.fml.loading.StringUtils;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileModLocator;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author DustW
 */
public class Locator extends AbstractJarFileModLocator {
    static final Logger LOGGER = LogUtils.getLogger();
    Path modFolder = FMLPaths.MODSDIR.get();

    @Override
    public String name() {
        return "CHMS Mod Locator";
    }

    @Override
    public void initArguments(Map<String, ?> arguments) {
        // nothing to do
    }

    Stream<Path> getAllChild() {
        ArrayList<Path> result = new ArrayList<>();
        this.scanFolders().forEach(folder -> {
            try {
                result.addAll(Files.list(folder.toPath()).toList());
            } catch (IOException var3) {
                var3.printStackTrace();
            }
        });

        return result.stream();
    }

    ArrayList<File> scanFolders() {
        ArrayList<File> result = new ArrayList<>();
        this.scanFolder(this.modFolder, result);
        return result;
    }

    Path scanFolder(Path folder, ArrayList<File> fileList) {
        try (var files = Files.list(folder)) {
            files.map(Path::toFile)
                    .filter(f -> f.isDirectory() && !f.getName().equals("disable"))
                    .map(file -> this.scanFolder(file.toPath(), fileList))
                    .forEach(file -> fileList.add(file.toFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return folder;
    }

    @Override
    public Stream<Path> scanCandidates() {
        LOGGER.debug("[CHMS] Scanning mods dir {} for mods", this.modFolder);
        List<Path> excluded = ModDirTransformerDiscoverer.allExcluded();
        return LamdbaExceptionUtils.uncheck(this::getAllChild)
                .filter(p -> !excluded.contains(p))
                .sorted(Comparator.comparing(path -> StringUtils.toLowerCase(path.getFileName().toString())))
                .filter(p -> StringUtils.toLowerCase(p.getFileName().toString()).endsWith(".jar"));
    }
}

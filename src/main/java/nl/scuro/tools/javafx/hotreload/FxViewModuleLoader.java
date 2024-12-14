package nl.scuro.tools.javafx.hotreload;

import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Set;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class FxViewModuleLoader extends URLClassLoader {

    URI pathUri;

    public FxViewModuleLoader(URI pathUri) throws MalformedURLException {
        super(new URL[] { pathUri.toURL() });
        this.pathUri = pathUri;
    }

    public Node loadFxView(String fqViewName) {
        try {
            ModuleFinder mf = ModuleFinder.of(Path.of(pathUri));
            String moduleName = mf.findAll().stream().map(mr -> mr.descriptor().name()).findFirst().get();

            Configuration config = ModuleLayer.boot().configuration().resolve(mf, ModuleFinder.of(),
                    Set.of(moduleName));
            ModuleLayer moduleLayer = ModuleLayer.boot().defineModulesWithOneLoader(config, this);
            System.out.println("--------------");
            moduleLayer.modules()
                    .stream()
                    .map(Module::getDescriptor) // get the descriptor of module
                    .map(ModuleDescriptor::requires) // set of requires in the module dependencies
                    .forEach(System.out::println);
            System.out.println("--------------");
            System.out.println("ModuleLayer: " + moduleLayer.configuration());
            final ClassLoader findLoader = moduleLayer.findLoader(moduleName);
            FXMLLoader.setDefaultClassLoader(findLoader);
            Class<?> clazz = findLoader.loadClass(fqViewName);
            return (Node) clazz.getDeclaredConstructor().newInstance();

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | ClassNotFoundException | InstantiationException ex) {
            ex.printStackTrace();
            return new Label("Could not load component: " + ex.getMessage());
        }
    }

}
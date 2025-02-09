/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.compiler.common;

import org.ballerinalang.langserver.compiler.LSCompilerUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Document class to hold the file path used in the LS.
 */
public class LSDocument {
    private Path path;
    private String uri;
    private String projectRoot;
    private List<String> projectModules = new ArrayList<>();
    private boolean withinProject = false;
    private String ownerModule = "";
    private Path ownerModulePath = null;

    public LSDocument(String uri) {
        try {
            this.uri = uri;
            this.path = Paths.get(new URL(uri).toURI());
            this.projectRoot = LSCompilerUtil.getProjectRoot(this.path);
            if (this.projectRoot == null) {
                return;
            }
            try {
                this.withinProject = !Files.isSameFile(this.path.getParent(), Paths.get(projectRoot));
            } catch (IOException e) {
                withinProject = false;
            }
            if (withinProject) {
                // TODO: Fix project module retrieve logic
                this.projectModules = this.getCurrentProjectModules(Paths.get(projectRoot));
                this.ownerModule = this.getModuleNameForDocument(this.projectRoot, path.toString());
                this.ownerModulePath = Paths.get(projectRoot).resolve("src").resolve(ownerModule);
            }
        } catch (URISyntaxException | MalformedURLException e) {
            // Ignore
        }
    }

    public LSDocument(Path path, String projectRoot) {
        this.uri = path.toUri().toString();
        this.projectRoot = projectRoot;
        this.path = path;
        this.withinProject = true;
    }

    /**
     * Get the path of the given URI.
     *
     * @return {@link Path} get the path
     */
    public Path getPath() {
        return this.path;
    }

    /**
     * Get source root path.
     *
     * @return {@link Path} source root path
     */
    public Path getProjectRootPath() {
        return Paths.get(this.projectRoot);
    }

    /**
     * Get the URI of the given string URI.
     *
     * @return {@link URI} get the URI
     * @throws MalformedURLException can throw malformed url exception
     * @throws URISyntaxException    can throw URI syntax exception
     */
    public URI getURI() throws MalformedURLException, URISyntaxException {
        return new URL(uri).toURI();
    }

    /**
     * Get the uri.
     *
     * @return {@link String} get the string uri
     */
    public String getURIString() {
        return this.uri;
    }

    /**
     * Get source root.
     *
     * @return {@link String} source root
     */
    public String getProjectRoot() {
        return this.projectRoot;
    }

    /**
     * Set URI.
     *
     * @param uri string URI
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Set source root.
     *
     * @param sourceRoot source root
     */
    public void setProjectRootRoot(String sourceRoot) {
        this.projectRoot = sourceRoot;
    }

    /**
     * Get the project modules list.
     * 
     * @return {@link List} list of project modules
     */
    public List<String> getProjectModules() {
        return projectModules;
    }

    public boolean isWithinProject() {
        return withinProject;
    }

    public String getOwnerModule() {
        return ownerModule;
    }

    public Path getOwnerModulePath() {
        return ownerModulePath;
    }

    @Override
    public String toString() {
        return "{" + "projectRoot:" + this.projectRoot + ", uri:" + this.uri + "}";
    }
    
    /**
     * Get the package name for given file.
     *
     * @param projectRoot project root
     * @param filePath full path of the file
     * @return {@link String} package name
     */
    private String getModuleNameForDocument(String projectRoot, String filePath) {
        String packageName = "";
        String packageStructure = filePath.substring(projectRoot.length() + 1);
        String[] splittedPackageStructure = packageStructure.split(Pattern.quote(File.separator));
        if (splittedPackageStructure.length > 0 && !splittedPackageStructure[0].endsWith(".bal")) {
            packageName = packageStructure.split(Pattern.quote(File.separator))[1];
        }
        return packageName;
    }

    /**
     * Get the list of module names in the repo.
     *
     * @param projectRoot project root path
     * @return {@link List} List of module names
     */
    private List<String> getCurrentProjectModules(Path projectRoot) {
        try {
            Stream<Path> pathStream = Files.walk(projectRoot.resolve("src"));
            return pathStream
                    .filter(path -> {
                        try {
                            return Files.isDirectory(path) && !Files.isHidden(path);
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}

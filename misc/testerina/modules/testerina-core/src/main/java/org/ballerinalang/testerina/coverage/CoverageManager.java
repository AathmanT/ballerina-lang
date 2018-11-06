/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.testerina.coverage;

import org.ballerinalang.bre.coverage.ExecutedInstruction;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.LineNumberInfoHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is singleton object which manages data and objects required for the coverage data and reports.
 *
 * @since 0.985
 */
public class CoverageManager {

    private static final Map<String, List<ExecutedInstruction>> executedInstructionOrderMap = new ConcurrentHashMap<>();

    // for the project there can be multiple modules and each will have programFile
    private static Map<String, ProgramFile> programFilesForProject;

    private static Map<String, LineNumberInfoHolder> lineNumberInfoHolderForProject = new HashMap<>();

    private static CoverageDataFormatter coverageDataFormatter = new CoverageDataFormatter();

    private static final CoverageManager coverageManger = new CoverageManager();

    private CoverageManager() {

    }

    public static CoverageManager getInstance() {

        return coverageManger;
    }

    /**
     * Setter for the program files for the project.
     *
     * @param programFilesForProject Map of program files from each module for the project
     */
    public static void setProgramFilesForProject(Map<String, ProgramFile> programFilesForProject) {

        CoverageManager.programFilesForProject = programFilesForProject;

        programFilesForProject.forEach((pkgPath, prjctProgramFile) -> {
            LineNumberInfoHolder lineNumberInfoHolder = new LineNumberInfoHolder();
            lineNumberInfoHolder.processPkgInfo(prjctProgramFile.getPackageInfoEntries());
            lineNumberInfoHolderForProject.put(pkgPath, lineNumberInfoHolder);
        });
    }

    /**
     * Getter for map of line number info for each module of the project.
     *
     * @return map of line number info for each module of the project
     */
    public static Map<String, LineNumberInfoHolder> getLineNumberInfoHolderForProject() {

        return lineNumberInfoHolderForProject;
    }

    /**
     * Getter for the map of execution data of each Ip from each module of the project.
     *
     * @return map of execution data of each Ip from each module of the project
     */
    public static Map<String, List<ExecutedInstruction>> getExecutedInstructionOrderMap() {

        return executedInstructionOrderMap;
    }

    /**
     * Getter for Ip coverage data into lcov coverage data formatter.
     *
     * @return Ip coverage data into lcov coverage data formatter
     */
    public static CoverageDataFormatter getCoverageDataFormatter() {

        return coverageDataFormatter;
    }

    /**
     * Getter for the program files map for each module.
     *
     * @return program files map for each module
     */
    public static Map<String, ProgramFile> getProgramFilesForProject() {

        return programFilesForProject;
    }
}

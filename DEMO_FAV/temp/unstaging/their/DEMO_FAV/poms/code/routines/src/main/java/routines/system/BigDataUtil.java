// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package routines.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
/**
 * created by rdubois on 19 juin 2015 Detailled comment
 *
 */
public class BigDataUtil {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BigDataUtil.class);

    private static final String HADOOP_HOME_DIR = "/winutils"; //$NON-NLS-1$

    private static final String WINUTILS_EXE = "winutils.exe"; //$NON-NLS-1$

    private static final String HADOOP_HOME_DIR_SYSPROP = "hadoop.home.dir"; //$NON-NLS-1$

    private static final String HADOOP_HOME_DIR_ENV = "HADOOP_HOME"; //$NON-NLS-1$

    private static String OS = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$

    private static boolean installHadoopHomeDirectory(java.io.File binFolder) {
        boolean folderCreated = true;
        if (!binFolder.exists()) {
            folderCreated = binFolder.mkdirs();
        }
        return folderCreated;
    }

    private static boolean environmentVariablesContainHadoopHomeDir() {
        boolean envExists = false;
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            if (HADOOP_HOME_DIR_ENV.equals(envName)) {
                envExists = true;
                break;
            }
        }
        return envExists;
    }

    private static boolean systemPropertiesContainHadoopHomeDir() {
        boolean sysPropExists = false;
        if (System.getProperties().containsKey(HADOOP_HOME_DIR_SYSPROP)) {
            sysPropExists = true;
        }
        return sysPropExists;
    }

    public static boolean installWinutils(String scratchdir, String winutilsFilePath) throws Exception {
        boolean isWindowsOS = OS.indexOf("win") >= 0; //$NON-NLS-1$
        if (isWindowsOS) {
            boolean environmentVariablesContainHadoopHomeDir = environmentVariablesContainHadoopHomeDir();
            boolean systemPropertiesContainHadoopHomeDir = systemPropertiesContainHadoopHomeDir();
            if (!environmentVariablesContainHadoopHomeDir && !systemPropertiesContainHadoopHomeDir) {
                LOG.debug(HADOOP_HOME_DIR_ENV + " not found in the environment variables."); //$NON-NLS-1$
                LOG.debug(HADOOP_HOME_DIR_SYSPROP + " not found in the system properties."); //$NON-NLS-1$
                java.io.File binFolder = new java.io.File(scratchdir + HADOOP_HOME_DIR + "/bin"); //$NON-NLS-1$
                boolean hadoopHomeDirectoryExists = installHadoopHomeDirectory(binFolder);
                if (hadoopHomeDirectoryExists) {
                    File winutil = new File(binFolder.getAbsolutePath() + "/" + WINUTILS_EXE); //$NON-NLS-1$
                    if (winutil != null && !winutil.exists()) {
                        java.nio.file.Files.copy(java.nio.file.FileSystems.getDefault().getPath(winutilsFilePath),
                                java.nio.file.FileSystems.getDefault().getPath(binFolder.getAbsolutePath() + "/" + WINUTILS_EXE)); //$NON-NLS-1$
                    }
                    System.setProperty(HADOOP_HOME_DIR_SYSPROP, new File(scratchdir + "/winutils").getAbsolutePath()); //$NON-NLS-1$
                    LOG.debug(HADOOP_HOME_DIR_SYSPROP + " = " + System.getProperty(HADOOP_HOME_DIR_SYSPROP)); //$NON-NLS-1$
                } else {
                    throw new Exception("Unable to install the hadoop home directory. Please do it by yourself."); //$NON-NLS-1$
                }
            } else if (!systemPropertiesContainHadoopHomeDir && environmentVariablesContainHadoopHomeDir) {
                LOG.debug(HADOOP_HOME_DIR_ENV + " found in the environment variables."); //$NON-NLS-1$
                LOG.debug(HADOOP_HOME_DIR_SYSPROP + " not found in the environment variables."); //$NON-NLS-1$
                String hadoopHomeDir = System.getenv(HADOOP_HOME_DIR_ENV);
                if (!new java.io.File(hadoopHomeDir + "/bin/" + WINUTILS_EXE).exists()) { //$NON-NLS-1$
                    throw new FileNotFoundException(
                            "The hadoop home directory (" + HADOOP_HOME_DIR_ENV + ") doesn't contain the required " //$NON-NLS-1$ //$NON-NLS-2$
                                    + WINUTILS_EXE + " binary."); //$NON-NLS-1$
                }
            } else {
                LOG.debug(HADOOP_HOME_DIR_ENV + " found in the environment variables."); //$NON-NLS-1$
                LOG.debug(HADOOP_HOME_DIR_SYSPROP + " found in the environment variables."); //$NON-NLS-1$s
                String hadoopHomeDir = System.getProperty(HADOOP_HOME_DIR_SYSPROP);
                if (!new java.io.File(hadoopHomeDir + "/bin/" + WINUTILS_EXE) //$NON-NLS-1$
                        .exists()) {
                    throw new FileNotFoundException(
                            "The hadoop home directory (" + HADOOP_HOME_DIR_SYSPROP + ") doesn't contain the required " //$NON-NLS-1$ //$NON-NLS-2$
                                    + WINUTILS_EXE + " binary."); //$NON-NLS-1$
                }
            }

        }
        return true;
    }

    /**

     * Create a context properties file in temporary directory and in case of
     * malicious values for job's name or context's name an exception is raised.
     * <br>
     * <b>if it exists then it is deleted and created </b>
     *
     * @param jobName - the name of the job
     * @param contextName - the name of the context without extension (.properties)
     * @return a context properties. Make sure that it is in tmpDir.
     * @throws Exception - an exception is raised when the file is not in temporary directory.
     */

    public static File createTempPropertiesFile(String jobName, String contextName) throws Exception {
    	File resultFile = new File(System.getProperty("java.io.tmpdir") + File.separatorChar + jobName
                + File.separatorChar + contextName + "_" + System.currentTimeMillis() + ".properties");
        if (isNotCorrectConfiguration(jobName,contextName,resultFile) ) {
            throw new Exception("Incorrect path,job's name or context's name may contain illegal characters.job's name :"
		+ jobName + " context's name : " + contextName);
	}
    	resultFile.getParentFile().mkdir();
        if(resultFile.exists()) { resultFile.delete(); }
        resultFile.createNewFile();
        return resultFile;
    }

    /**
     * fill temporary file with  data from an InputStream.
     * @param tmpFile - Temporary file to fill
     * @param fileSource - data to fill in temp file
     * @throws java.lang.Exception : an exception is raised when the temporary file path is not in temporary directory or if path is not correct.
     */
    public static void fillTempFile(java.io.File tmpFile,java.io.InputStream contextIn )throws java.lang.Exception {
    	if(tmpFile != null && isInTemp(tmpFile)) {
    		try(java.io.OutputStream contextOut = new java.io.FileOutputStream(tmpFile)) {
    	 	   	int len = -1;
    	 	    byte[] b = new byte[4096];
    	 	    while ((len = contextIn.read(b)) != -1) {
    	 	        contextOut.write(b, 0, len);
    	 	    }
    	 	 }
    	} else {
    		throw new Exception("Incorrect temp path file :" + tmpFile);
    	}
     }
    private static boolean isNotCorrectConfiguration(String jobName, String contextName,File fileTemp) throws IOException {
    	return jobName == null || contextName == null || isNotInTemp(fileTemp);
    }
    private static boolean isInTemp(File file) throws IOException {
        String javaTmpPath = (new File(System.getProperty("java.io.tmpdir"))).getCanonicalPath();
        return file.getCanonicalPath().startsWith(javaTmpPath);
    }
    private static boolean isNotInTemp(File file) throws IOException {
    	return !isInTemp(file);
    }
}

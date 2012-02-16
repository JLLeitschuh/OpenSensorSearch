/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.sir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.UnavailableException;

import org.n52.sir.catalog.ICatalogConnection;
import org.n52.sir.catalog.ICatalogFactory;
import org.n52.sir.catalog.ICatalogStatusHandler;
import org.n52.sir.decode.IHttpGetRequestDecoder;
import org.n52.sir.decode.IHttpPostRequestDecoder;
import org.n52.sir.ds.IConnectToCatalogDAO;
import org.n52.sir.ds.IDAOFactory;
import org.n52.sir.listener.ISirRequestListener;
import org.n52.sir.ows.OwsExceptionReport;
import org.n52.sir.ows.OwsExceptionReport.ExceptionCode;
import org.n52.sir.ows.OwsExceptionReport.ExceptionLevel;
import org.n52.sir.util.jobs.IJobScheduler;
import org.n52.sir.util.jobs.IJobSchedulerFactory;
import org.n52.sir.util.jobs.impl.TimerServlet;
import org.n52.sir.xml.ITransformerFactory;
import org.n52.sir.xml.IValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniMuenster.swsl.sir.CapabilitiesDocument;
import de.uniMuenster.swsl.sir.VersionAttribute;
import de.uniMuenster.swsl.sir.VersionAttribute.Version.Enum;

/**
 * Singleton class reads the config file and builds the RequestOperator and DAO
 * 
 * @author Jan Schulte
 * 
 */
public class SirConfigurator {

    /** Sections for the Capabilities */
    public enum Section {
        ServiceIdentification, ServiceProvider, OperationsMetadata, Contents, All
    }

    /**
     * propertyname of character encoding
     */
    private static final String CHARACTER_ENCODING = "CHARACTERENCODING";

    /**
     * propertyname of get request decoder
     */
    private static final String GETREQUESTDECODER = "GETREQUESTDECODER";

    /**
     * propertyname of post request decoder
     */
    private static final String POSTREQUESTDECODER = "POSTREQUESTDECODER";

    /**
     * propertyname of DAOFACTORY property
     */
    private static final String DAOFACTORY = "DAOFACTORY";

    /**
     * propertyname of STATUS_HANDLER property
     */
    private static final String STATUS_HANDLER = "STATUS_HANDLER";

    /**
     * propertyname of CATALOGFACTORY property
     */
    private static final String CATALOGFACTORY = "CATALOGFACTORY";

    /**
     * propertyname of JOBSCHEDULERFACTORY property
     */
    private static final String JOBSCHEDULERFACTORY = "JOBSCHEDULERFACTORY";

    /**
     * propertyname of TRANSFORMERFACTORY property
     */
    private static final String TRANSFORMERFACTORY = "TRANSFORMERFACTORY";

    /**
     * Delay (in seconds) for scheduling the first run of repeated catalog connections on startup.
     */
    private static final int STARTUP_CATALOG_CONNECTION_DELAY_SECS = 10;

    /**
     * propertyname of SCHEDULE_JOBS_ON_STARTUP property
     */
    private static final String SCHEDULE_JOBS_ON_STARTUP = "SCHEDULE_JOBS_ON_STARTUP";

    /**
     * propertyname of CLASSIFICATION_INIT_FILENAME property
     */
    private static final String CLASSIFICATION_INIT_FILENAMES = "CLASSIFICATION_INIT_FILENAMES";

    /**
     * propertyname of SLOT_INIT_FILENAME property
     */
    private static final String SLOT_INIT_FILENAME = "SLOT_INIT_FILENAME";

    /**
     * propertyname of UNCHECKED_CATALOGS property
     */
    private static final String DO_NOT_CHECK_CATALOGS = "DO_NOT_CHECK_CATALOGS";

    /**
     * the separator for config properties with more than one value
     */
    private static final String CONFIG_FILE_LIST_SEPARATOR = ",";

    /**
     * 
     */
    private static final String SCHEMA_URL = "SCHEMA_URL";

    /**
     * 
     */
    private static final String NAMESPACE_URI = "NAMESPACE_URI";

    /**
     * 
     */
    private static final String NAMESPACE_PREFIX = "NAMESPACE_PREFIX";

    /**
     * propertyname of listeners
     */
    private static final String LISTENERS = "LISTENERS";

    /**
     * propertyname of CONFIG_DIRECTORY property
     */
    private static final String CONFIG_DIRECTORY = "CONFIG_DIRECTORY";

    /**
     * propertyname of XSLT_DIR property
     */
    private static final String XSTL_DIRECTORY = "XSTL_DIRECTORY";

    /**
     * propertyname of capabilities skeleton
     */
    private static final String CAPABILITIESSKELETON_FILENAME = "CAPABILITIESSKELETON_FILENAME";

    /**
     * propertyname of the SIR service version
     */
    private static final String SERVICEVERSION = "SERVICEVERSION";

    /**
     * propertyname of GML date format
     */
    private static final String GMLDATEFORMAT = "GMLDATEFORMAT";

    /**
     * propertyname of service url
     */
    private static final String SERVICEURL = "SERVICEURL";

    /**
     * propertyname of test requests
     */
    private static final String TESTREQUESTS = "TESTREQUESTS";

    /**
     * propertyname of discovery profile file
     */
    private static final String PROFILE4DISCOVERY = "PROFILE4DISCOVERY";

    /**
     * propertyname of discovery profile file
     */
    private static final String SVRL_SCHEMA = "SVRL_SCHEMA";

    /**
     * 
     */
    private static final String VALIDATORFACTORY = "VALIDATORFACTORY";

    /**
     * 
     */
    private static final String SCHEMATRON_DOWNLOAD = "SCHEMATRON_DOWNLOAD";

    /**
     * 
     */
    private static final String EXTENDED_DEBUG_TO_CONSOLE = "EXTENDED_DEBUG_TO_CONSOLE";

    /**
     * 
     */
    private static final String ACCEPTED_SERVICE_VERSIONS = "ACCEPTED_SERVICE_VERSIONS";

    /**
     * 
     */
    private static final String VERSION_SPLIT_CHARACTER = ",";

    /**
     * 
     */
    private static final String VALIDATE_XML_REQUESTS = "VALIDATE_XML_REQUESTS";

    /**
     * 
     */
    private static final String VALIDATE_XML_RESPONSES = "VALIDATE_XML_RESPONSES";

    /**
     * logger
     */
    protected static Logger log = LoggerFactory.getLogger(SirConfigurator.class);

    /**
     * instance attribute, due to the singleton pattern
     */
    private static SirConfigurator instance = null;

    /**
     * base path for configuration file
     */
    private String basepath;

    /**
     * properties for DAO implementation
     */
    private Properties daoProps;

    /**
     * common SIR properties
     */
    private Properties props;

    /**
     * character encoding for responses
     */
    private String characterEncoding;

    /**
     * decoder for decoding httpPost request
     */
    private IHttpPostRequestDecoder httpPostDecoder;

    /**
     * decoder for decoding httpGet request
     */
    private IHttpGetRequestDecoder httpGetDecoder;

    /**
     * Implementation of the DAOFactory, used to build the DAOs for the request listeners
     */
    private IDAOFactory factory;

    /**
     * servlet for scheduling tasks
     */
    private TimerServlet timerServlet;

    /**
     * Implementation of IJobSchedulerFactory to schedule (repeated) tasks
     */
    private IJobSchedulerFactory jobSchedulerFactory;

    /**
     * Implementation of the ITransformerFactory, used to access transformers for XML documents
     */
    private ITransformerFactory transformerFactory;

    /**
     * The constructor for the catalog factory to create catalogs for give URLs on demand
     */
    private Constructor<ICatalogFactory> catalogFactoryConstructor;

    /**
     * Implementation of ICatalogStatusHandler to allow other servlets to change stati of catalogs in the
     * database
     */
    private ICatalogStatusHandler catalogStatusHandler;

    /**
     * The skeleton of a standart capabilities response document
     */
    private CapabilitiesDocument capabilitiesSkeleton;

    /**
     * Service version of the SIR
     */
    private String serviceVersion;

    /**
     * update sequence
     */
    private String updateSequence;

    /**
     * GML date format
     */
    private String gmlDateFormat;

    /**
     * 
     */
    private String configDirectory;

    /**
     * file names of catalog initialization files, i.e. files that need to be loaded into the catalog prior to
     * usage.
     */
    private String[] catalogInitClassificationFiles;

    /**
     * file name of a catalog initialization file
     */
    private String catalogInitSlotFile;

    /**
     * the url of the service, e.g. needed by test client
     */
    private URL serviceUrl;

    /**
     * a list of catalogue-URLs that are not checked when data is pushed into them
     */
    private ArrayList<URL> doNotCheckCatalogsList;

    private String schemaUrl;

    private String namespaceUri;

    private String namespacePrefix;

    private String testRequestPath;

    private String profile4Discovery;

    private IValidatorFactory validatorFactory;

    private String svrlSchema;

    private boolean extendedDebugToConsole;

    private String[] acceptedVersions;

    private static final int THREAD_POOL_SIZE = 10;

    private ExecutorService exec;

    private boolean validateRequests;

    private boolean validateResponses;

    /**
     * private constructor due to the singleton pattern.
     * 
     * @param configStream
     *        Inputstream of the configfile
     * @param dbConfigStream
     *        Inputstream of the db configfile
     * @param basepath
     *        base path for configuration files
     * @param xsltDir
     * 
     */
    private SirConfigurator(InputStream configStream,
                            InputStream dbConfigStream,
                            String basepath,
                            TimerServlet timerServlet) throws UnavailableException {
        try {
            this.basepath = basepath;
            this.timerServlet = timerServlet;

            // creating common SIR properties object from inputstream
            this.props = loadProperties(configStream);
            this.daoProps = loadProperties(dbConfigStream);

            log.info(" ***** Config Files loaded successfully! ***** ");
        }
        catch (IOException ioe) {
            log.error("Error while loading config file.", ioe);
            throw new UnavailableException(ioe.getMessage());
        }
    }

    /**
     * method loads the config file
     * 
     * @param is
     *        InputStream containing the config file
     * @return Returns properties of the given config file
     * @throws IOException
     */
    private Properties loadProperties(InputStream is) throws IOException {
        Properties properties = new Properties();
        properties.load(is);

        return properties;
    }

    /**
     * @param configStream
     * @param dbConfigStream
     * @param basepath
     * @return Returns an instance of the SirConfigurator. This method is used to implement the singleton
     *         pattern
     * @throws UnavailableException
     *         if the configFile could not be loaded
     * @throws OwsExceptionReport
     */
    public synchronized static SirConfigurator getInstance(InputStream configStream,
                                                           InputStream dbConfigStream,
                                                           String basepath,
                                                           TimerServlet timerServlet) throws UnavailableException,
            OwsExceptionReport {
        if (instance == null) {
            instance = new SirConfigurator(configStream, dbConfigStream, basepath, timerServlet);
            instance.initialize();
        }
        return instance;
    }

    private void initialize() throws OwsExceptionReport {
        log.info(" * Initializing SirConfigurator ... ");

        // to be used by listeners, saved here to allow shutdown.
        this.exec = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        String characterEncodingString = this.props.getProperty(CHARACTER_ENCODING);
        if (characterEncodingString == null) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(ExceptionCode.NoApplicableCode,
                                 null,
                                 "No characterEncoding is defined in the config file!");
            log.error("No characterEncoding is defined in the config file!");
            throw se;
        }

        this.characterEncoding = characterEncodingString;
        this.serviceVersion = this.props.getProperty(SERVICEVERSION);
        this.gmlDateFormat = this.props.getProperty(GMLDATEFORMAT);
        this.configDirectory = this.props.getProperty(CONFIG_DIRECTORY);
        this.schemaUrl = this.props.getProperty(SCHEMA_URL);
        this.namespaceUri = this.props.getProperty(NAMESPACE_URI);
        this.namespacePrefix = this.props.getProperty(NAMESPACE_PREFIX);
        this.testRequestPath = this.basepath + this.props.getProperty(TESTREQUESTS);

        this.extendedDebugToConsole = Boolean.parseBoolean(this.props.getProperty(EXTENDED_DEBUG_TO_CONSOLE));
        this.acceptedVersions = this.props.getProperty(ACCEPTED_SERVICE_VERSIONS).split(VERSION_SPLIT_CHARACTER);
        this.validateRequests = Boolean.parseBoolean(this.props.getProperty(VALIDATE_XML_REQUESTS));
        this.validateResponses = Boolean.parseBoolean(this.props.getProperty(VALIDATE_XML_RESPONSES));

        String resourceName = this.props.getProperty(PROFILE4DISCOVERY);
        URL location = this.getClass().getResource(resourceName);
        if (location == null) {
            log.error("Could not get resource using class loader!");
            throw new OwsExceptionReport(ExceptionCode.NoApplicableCode,
                                         "root",
                                         "Could not get resource using class loader: " + resourceName);
        }

        this.profile4Discovery = location.getPath();
        checkFile(this.profile4Discovery);

        resourceName = this.props.getProperty(SVRL_SCHEMA);
        location = this.getClass().getResource(resourceName);
        if (location == null) {
            log.error("Could not get resource using class loader!");
            throw new OwsExceptionReport(ExceptionCode.NoApplicableCode,
                                         "root",
                                         "Could not get resource using class loader: " + resourceName);
        }

        this.svrlSchema = location.getPath();
        checkFile(this.svrlSchema);

        String url = null;
        try {
            url = this.props.getProperty(SERVICEURL);
            this.serviceUrl = new URL(url);
        }
        catch (MalformedURLException e) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(ExceptionCode.NoApplicableCode,
                                 null,
                                 "No valid service url is defined in the config file: " + url);
            log.error("No valid service url is defined in the config file: " + url);
            throw se;
        }

        // set updateSequence
        newUpdateSequence();

        // initialize DAO Factory
        initializeDAOFactory(this.daoProps);

        // initialize CatalogFactory
        initializeCatalogFactory(this.props);

        // initialize decoders
        initializeHttpGetRequestDecoder(this.props);
        initializeHttpPostRequestDecoder(this.props);

        loadCapabilitiesSkeleton(this.props);

        // initialize status handler
        initializeStatusHandler(this.props);

        // initialize job scheduler and start the connections from database
        initializeJobScheduling(this.props, this.timerServlet);
        if (Boolean.parseBoolean(this.props.getProperty(SCHEDULE_JOBS_ON_STARTUP)))
            startCatalogConnections();

        // initialize transformer
        initializeTransformerFactory(this.props);

        // initialize validator
        initializeValidatorFactory(this.props);

        log.info(" ***** Initialized SirConfigurator successfully! ***** ");
    }

    /**
     * 
     * @param resourcePath
     */
    @SuppressWarnings("unused")
    private void checkResource(String resourcePath) {
        InputStream resource = SirConfigurator.class.getResourceAsStream(resourcePath);

        if (resource != null) {
            try {
                int i = resource.read();
                if (i == -1)
                    log.error("Resource is empty.");
            }
            catch (IOException e) {
                log.error("Cannot read resource " + resourcePath);
            }
        }
        else
            log.error("Cannot find resource " + resourcePath);
    }

    /**
     * 
     * @param path
     */
    private void checkFile(String path) {
        File f = new File(path);
        if ( !f.exists())
            log.error("Cannot find file " + path);

        f = null;
    }

    private void loadCapabilitiesSkeleton(Properties sirProps) throws OwsExceptionReport {
        String skeletonPath = sirProps.getProperty(CAPABILITIESSKELETON_FILENAME);
        InputStream resource = SirConfigurator.class.getResourceAsStream(skeletonPath);

        log.info("Loading capabilities skeleton from " + skeletonPath);

        try {
            this.capabilitiesSkeleton = CapabilitiesDocument.Factory.parse(resource);
        }
        catch (Exception e) {
            log.error("Error on loading capabilities skeleton file: " + e.getMessage());
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionReport.ExceptionCode.NoApplicableCode,
                                 null,
                                 "Error on loading capabilities skeleton file: " + e.getMessage());
            throw se;
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeDAOFactory(Properties daoPropsP) throws OwsExceptionReport {
        try {
            String daoName = this.props.getProperty(DAOFACTORY);

            if (daoName == null) {
                log.error("No DAOFactory Implementation is set in the config file!");
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.NoApplicableCode,
                                     "SosConfigurator.initializeDAOFactory()",
                                     "No DAOFactory Implementation is set in the configFile!");
                throw se;
            }

            // get Class of the DAOFactory Implementation
            Class<IDAOFactory> daoFactoryClass = (Class<IDAOFactory>) Class.forName(daoName);

            // types of the constructor arguments
            Class< ? >[] constrArgs = {Properties.class};

            Object[] args = {daoPropsP};

            // get Constructor of this class with matching parameter types
            Constructor<IDAOFactory> constructor = daoFactoryClass.getConstructor(constrArgs);

            this.factory = constructor.newInstance(args);

            log.info(" ***** " + daoName + " loaded successfully! ***** ");

        }
        catch (ClassNotFoundException cnfe) {
            log.error("Error while loading DaoFactory, required class could not be loaded: " + cnfe.toString());
            throw new OwsExceptionReport(cnfe.getMessage(), cnfe.getCause());
        }
        catch (SecurityException se) {
            log.error("Error while loading DAOFactory: " + se.toString());
            throw new OwsExceptionReport(se.getMessage(), se.getCause());
        }
        catch (NoSuchMethodException nsme) {
            log.error("Error while loading DAOFactory, no required constructor available: ");
            throw new OwsExceptionReport(nsme.getMessage(), nsme.getCause());
        }
        catch (IllegalArgumentException iae) {
            log.error("Error while loading DAOFactory, parameters for the constructor are illegal: " + iae.toString());
            throw new OwsExceptionReport(iae.getMessage(), iae.getCause());
        }
        catch (InstantiationException ie) {
            log.error("The instantiation of a DAOFactory failed: " + ie.toString());
            throw new OwsExceptionReport(ie.getMessage(), ie.getCause());
        }
        catch (IllegalAccessException iace) {
            log.error("The instantiation of a DAOFactory failed: " + iace.toString());
            throw new OwsExceptionReport(iace.getMessage(), iace.getCause());
        }
        catch (InvocationTargetException ite) {
            log.error("The instantiation of a DAOFactory failed: " + ite.toString());
            throw new OwsExceptionReport(ite.getMessage(), ite.getCause());
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeHttpGetRequestDecoder(Properties sirProps) throws OwsExceptionReport {
        String className = sirProps.getProperty(GETREQUESTDECODER);
        try {

            if (className == null) {
                log.error("No getRequestDecoder Implementation is set in the config file!");
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.NoApplicableCode,
                                     "SirConfigurator.initializeHttpGetRequestDecoder()",
                                     "No getRequestDecoder Implementation is set in the config file!");
                throw se;
            }
            // get Class of the httpGetRequestDecoderClass Implementation
            Class<IHttpGetRequestDecoder> httpGetRequestDecoderClass = (Class<IHttpGetRequestDecoder>) Class.forName(className);

            // get Constructor of this class with matching parameter types
            Constructor<IHttpGetRequestDecoder> constructor = httpGetRequestDecoderClass.getConstructor();

            this.httpGetDecoder = constructor.newInstance();

            log.info(" ***** " + className + " loaded successfully! ***** ");

        }
        catch (ClassNotFoundException cnfe) {
            log.error("Error while loading getRequestDecoder, required class could not be loaded: " + cnfe.toString());
            throw new OwsExceptionReport(cnfe.getMessage(), cnfe.getCause());
        }
        catch (NoSuchMethodException nsme) {
            log.error("Error while loading getRequestDecoder, no required constructor available: " + nsme.toString());
            throw new OwsExceptionReport(nsme.getMessage(), nsme.getCause());
        }
        catch (InstantiationException ie) {
            log.error("The instatiation of a getRequestDecoder failed: " + ie.toString());
            throw new OwsExceptionReport(ie.getMessage(), ie.getCause());

        }
        catch (IllegalAccessException iace) {
            log.error("The instatiation of an getRequestDecoder failed: " + iace.toString());
            throw new OwsExceptionReport(iace.getMessage(), iace.getCause());
        }
        catch (InvocationTargetException ite) {
            log.error("the instatiation of an getRequestDecoder failed: " + ite.toString() + ite.getLocalizedMessage()
                    + ite.getCause());
            throw new OwsExceptionReport(ite.getMessage(), ite.getCause());
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeHttpPostRequestDecoder(Properties sirProps) throws OwsExceptionReport {
        String className = sirProps.getProperty(POSTREQUESTDECODER);
        try {

            if (className == null) {
                log.error("No postRequestDecoder Implementation is set in the config file!");
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.NoApplicableCode,
                                     "SirConfigurator.initializeHttpPostRequestDecoder()",
                                     "No postRequestDecoder Implementation is set in the config file!");
                throw se;
            }
            // get Class of the httpGetRequestDecoderClass Implementation
            Class<IHttpPostRequestDecoder> httpPostRequestDecoderClass = (Class<IHttpPostRequestDecoder>) Class.forName(className);

            // get Constructor of this class with matching parameter types
            Constructor<IHttpPostRequestDecoder> constructor = httpPostRequestDecoderClass.getConstructor();

            this.httpPostDecoder = constructor.newInstance();

            log.info(" ***** " + className + " loaded successfully! ***** ");

        }
        catch (ClassNotFoundException cnfe) {
            log.error("Error while loading getRequestDecoder, required class could not be loaded: " + cnfe.toString());
            throw new OwsExceptionReport(cnfe.getMessage(), cnfe.getCause());
        }
        catch (NoSuchMethodException nsme) {
            log.error("Error while loading getRequestDecoder, no required constructor available: " + nsme.toString());
            throw new OwsExceptionReport(nsme.getMessage(), nsme.getCause());
        }
        catch (InstantiationException ie) {
            log.error("The instatiation of a getRequestDecoder failed: " + ie.toString());
            throw new OwsExceptionReport(ie.getMessage(), ie.getCause());
        }
        catch (IllegalAccessException iace) {
            log.error("The instatiation of an getRequestDecoder failed: " + iace.toString());
            throw new OwsExceptionReport(iace.getMessage(), iace.getCause());
        }
        catch (InvocationTargetException ite) {
            log.error("the instatiation of an getRequestDecoder failed: " + ite.toString() + ite.getLocalizedMessage()
                    + ite.getCause());
            throw new OwsExceptionReport(ite.getMessage(), ite.getCause());
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeCatalogFactory(Properties sirProps) throws OwsExceptionReport {
        String className = sirProps.getProperty(CATALOGFACTORY);

        this.catalogInitSlotFile = this.basepath + this.configDirectory + sirProps.getProperty(SLOT_INIT_FILENAME);

        // add classification init files
        String[] splitted = sirProps.getProperty(CLASSIFICATION_INIT_FILENAMES).split(CONFIG_FILE_LIST_SEPARATOR);
        this.catalogInitClassificationFiles = new String[splitted.length];
        for (int i = 0; i < splitted.length; i++) {
            this.catalogInitClassificationFiles[i] = this.basepath + this.configDirectory + splitted[i].trim();
        }

        // check if given url does not need to be checked
        this.doNotCheckCatalogsList = new ArrayList<URL>();
        splitted = sirProps.getProperty(DO_NOT_CHECK_CATALOGS).split(CONFIG_FILE_LIST_SEPARATOR);
        if (splitted.length > 0) {
            for (String s : splitted) {
                try {
                    if ( !s.isEmpty())
                        this.doNotCheckCatalogsList.add(new URL(s.trim()));
                }
                catch (MalformedURLException e) {
                    log.error("Could not parse catalog url to 'do not check' list. Catalog will be checked during runtime!");
                }
            }
        }
        else {
            if (log.isDebugEnabled())
                log.debug("Property " + DO_NOT_CHECK_CATALOGS + " returned no string list.");
        }

        try {

            if (className == null) {
                log.error("No catalog factory implementation is set in the config file! Use " + CATALOGFACTORY);
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.NoApplicableCode,
                                     "SirConfigurator.initializeCatalogFactory()",
                                     "No catalog factory Implementation is set in the config file!");
                throw se;
            }
            // get Class of the catalog factory implementation
            Class<ICatalogFactory> catalogFactoryClass = (Class<ICatalogFactory>) Class.forName(className);

            // get constructor of this class with matching parameter types
            this.catalogFactoryConstructor = catalogFactoryClass.getConstructor(URL.class,
                                                                                String[].class,
                                                                                String.class,
                                                                                Boolean.class);

            log.info(" ***** " + className + " loaded successfully! ***** ");
        }
        catch (ClassNotFoundException cnfe) {
            log.error("Error while loading catalog factory, required class could not be loaded: " + cnfe.toString());
            throw new OwsExceptionReport(cnfe.getMessage(), cnfe.getCause());
        }
        catch (NoSuchMethodException nsme) {
            log.error("Error while loading catalog factory, no required constructor available: " + nsme.toString());
            throw new OwsExceptionReport(nsme.getMessage(), nsme.getCause());
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeStatusHandler(Properties sirProps) throws OwsExceptionReport {
        String className = sirProps.getProperty(STATUS_HANDLER);
        try {
            if (className == null) {
                log.error("No status handler implementation is set in the config file!");
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.NoApplicableCode,
                                     "SirConfigurator.initializeStatusHandler()",
                                     "No status handler implementation is set in the config file!");
                throw se;
            }
            // get Class of the httpGetRequestDecoderClass Implementation
            Class<ICatalogStatusHandler> CatalogStatusHandlerClass = (Class<ICatalogStatusHandler>) Class.forName(className);

            // get Constructor of this class with matching parameter types
            Constructor<ICatalogStatusHandler> constructor = CatalogStatusHandlerClass.getConstructor();

            this.catalogStatusHandler = constructor.newInstance();

            log.info(" ***** " + className + " loaded successfully! ***** ");
        }
        catch (NoSuchMethodException nsme) {
            log.error("Error while loading catalogStatusHandler, no required constructor available: " + nsme.toString());
            throw new OwsExceptionReport(nsme.getMessage(), nsme.getCause());
        }
        catch (InstantiationException ie) {
            log.error("The instatiation of a catalogStatusHandler failed: " + ie.toString());
            throw new OwsExceptionReport(ie.getMessage(), ie.getCause());
        }
        catch (IllegalAccessException iae) {
            log.error("The instatiation of an catalogStatusHandler failed: " + iae.toString());
            throw new OwsExceptionReport(iae.getMessage(), iae.getCause());
        }
        catch (InvocationTargetException ite) {
            log.error("The instatiation of an catalogStatusHandler failed: " + ite.toString()
                    + ite.getLocalizedMessage() + ite.getCause());
            throw new OwsExceptionReport(ite.getMessage(), ite.getCause());
        }
        catch (ClassNotFoundException cnfe) {
            log.error("Error while loading catalogStatusHandler, required class could not be loaded: "
                    + cnfe.toString());
            throw new OwsExceptionReport(cnfe.getMessage(), cnfe.getCause());
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeJobScheduling(Properties p, TimerServlet timer) throws OwsExceptionReport {
        String className = p.getProperty(JOBSCHEDULERFACTORY);
        try {
            if (className == null) {
                log.error("No job scheduler factory implementation is set in the config file!");
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.NoApplicableCode,
                                     "SirConfigurator.initializeJobScheduling()",
                                     "No job scheduling implementation is set in the config file!");
                throw se;
            }
            // get Class of the httpGetRequestDecoderClass Implementation
            Class<IJobSchedulerFactory> jobSchedulerFactoryClass = (Class<IJobSchedulerFactory>) Class.forName(className);

            // get Constructor of this class with matching parameter types
            Constructor<IJobSchedulerFactory> constructor = jobSchedulerFactoryClass.getConstructor(TimerServlet.class);

            this.jobSchedulerFactory = constructor.newInstance(timer);

            log.info(" ***** " + className + " loaded successfully! ***** ");
        }
        catch (NoSuchMethodException nsme) {
            log.error("Error while loading jobSchedulerFactoryClass, no required constructor available: "
                    + nsme.toString());
            throw new OwsExceptionReport(nsme.getMessage(), nsme.getCause());
        }
        catch (InstantiationException ie) {
            log.error("The instatiation of a jobSchedulerFactoryClass failed: " + ie.toString());
            throw new OwsExceptionReport(ie.getMessage(), ie.getCause());
        }
        catch (IllegalAccessException iae) {
            log.error("The instatiation of an jobSchedulerFactoryClass failed: " + iae.toString());
            throw new OwsExceptionReport(iae.getMessage(), iae.getCause());
        }
        catch (InvocationTargetException ite) {
            log.error("The instatiation of an jobSchedulerFactoryClass failed: " + ite.toString()
                    + ite.getLocalizedMessage() + ite.getCause());
            throw new OwsExceptionReport(ite.getMessage(), ite.getCause());
        }
        catch (ClassNotFoundException cnfe) {
            log.error("Error while loading jobSchedulerFactoryClass, required class could not be loaded: "
                    + cnfe.toString());
            throw new OwsExceptionReport(cnfe.getMessage(), cnfe.getCause());
        }
    }

    /**
     * 
     * @param p
     * @throws OwsExceptionReport
     */
    @SuppressWarnings("unchecked")
    private void initializeTransformerFactory(Properties p) throws OwsExceptionReport {
        String xsltDir = p.getProperty(XSTL_DIRECTORY);
        String className = p.getProperty(TRANSFORMERFACTORY);
        try {
            if (className == null) {
                log.error("No transformer factory implementation is set in the config file!");
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.NoApplicableCode,
                                     "SirConfigurator.initializeHttpPostRequestDecoder()",
                                     "No postRequestDecoder Implementation is set in the config file!");
                throw se;
            }
            // get Class of the httpGetRequestDecoderClass Implementation
            Class<ITransformerFactory> transformerFactoryClass = (Class<ITransformerFactory>) Class.forName(className);

            // get Constructor of this class with matching parameter types
            Constructor<ITransformerFactory> constructor = transformerFactoryClass.getConstructor(String.class);

            this.transformerFactory = constructor.newInstance(this.basepath + xsltDir);

            log.info(" ***** " + className + " loaded successfully! Using files from folder " + this.basepath + xsltDir
                    + ". ***** ");
        }
        catch (NoSuchMethodException nsme) {
            log.error("Error while loading transformerFactoryClass, no required constructor available: "
                    + nsme.toString());
            throw new OwsExceptionReport(nsme.getMessage(), nsme.getCause());
        }
        catch (InstantiationException ie) {
            log.error("The instatiation of a transformerFactoryClass failed: " + ie.toString());
            throw new OwsExceptionReport(ie.getMessage(), ie.getCause());
        }
        catch (IllegalAccessException iae) {
            log.error("The instatiation of an transformerFactoryClass failed: " + iae.toString());
            throw new OwsExceptionReport(iae.getMessage(), iae.getCause());
        }
        catch (InvocationTargetException ite) {
            log.error("The instatiation of an transformerFactoryClass failed: " + ite.toString()
                    + ite.getLocalizedMessage() + ite.getCause());
            throw new OwsExceptionReport(ite.getMessage(), ite.getCause());
        }
        catch (ClassNotFoundException cnfe) {
            log.error("Error while loading transformerFactoryClass, required class could not be loaded: "
                    + cnfe.toString());
            throw new OwsExceptionReport(cnfe.getMessage(), cnfe.getCause());
        }
    }

    /**
     * 
     * @param p
     * @throws OwsExceptionReport
     */
    @SuppressWarnings("unchecked")
    private void initializeValidatorFactory(Properties p) throws OwsExceptionReport {
        String className = p.getProperty(VALIDATORFACTORY);
        try {
            if (className == null) {
                log.error("No validator factory implementation is set in the config file!");
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.NoApplicableCode,
                                     "SirConfigurator.initializeHttpPostRequestDecoder()",
                                     "No postRequestDecoder Implementation is set in the config file!");
                throw se;
            }
            // get Class of the httpGetRequestDecoderClass Implementation
            Class<IValidatorFactory> validatorFactoryClass = (Class<IValidatorFactory>) Class.forName(className);

            // get Constructor of this class with matching parameter types
            Constructor<IValidatorFactory> constructor = validatorFactoryClass.getConstructor();

            this.validatorFactory = constructor.newInstance();

            log.info(" ***** " + className + " loaded successfully! ***** ");
        }
        catch (NoSuchMethodException nsme) {
            log.error("Error while loading validatorFactoryClass, no required constructor available: "
                    + nsme.toString());
            throw new OwsExceptionReport(nsme.getMessage(), nsme.getCause());
        }
        catch (InstantiationException ie) {
            log.error("The instatiation of a validatorFactoryClass failed: " + ie.toString());
            throw new OwsExceptionReport(ie.getMessage(), ie.getCause());
        }
        catch (IllegalAccessException iae) {
            log.error("The instatiation of an validatorFactoryClass failed: " + iae.toString());
            throw new OwsExceptionReport(iae.getMessage(), iae.getCause());
        }
        catch (InvocationTargetException ite) {
            log.error("The instatiation of an validatorFactoryClass failed: " + ite.toString()
                    + ite.getLocalizedMessage() + ite.getCause());
            throw new OwsExceptionReport(ite.getMessage(), ite.getCause());
        }
        catch (ClassNotFoundException cnfe) {
            log.error("Error while loading validatorFactoryClass, required class could not be loaded: "
                    + cnfe.toString());
            throw new OwsExceptionReport(cnfe.getMessage(), cnfe.getCause());
        }
    }

    /**
     * 
     * @return
     * @throws OwsExceptionReport
     */
    public RequestOperator buildRequestOperator() throws OwsExceptionReport {

        // initialize RequestOperator
        RequestOperator ro = new RequestOperator();

        // loading names of listeners
        ArrayList<String> listeners = loadListeners();

        Iterator<String> iter = listeners.iterator();

        // initialize listeners and add them to the RequestOperator
        while (iter.hasNext()) {

            // classname of the listener
            String classname = iter.next();

            try {
                // get Class of the Listener
                @SuppressWarnings("unchecked")
                Class<ISirRequestListener> listenerClass = (Class<ISirRequestListener>) Class.forName(classname);

                Class< ? >[] constrArgs = {};

                Object[] args = {};

                // get Constructor of this class with matching parameter types,
                // throws a NoSuchMethodException
                Constructor<ISirRequestListener> constructor = listenerClass.getConstructor(constrArgs);

                // add new requestListener to RequestOperator throws a
                // Instantiation, IllegalAccess and InvocationTargetException
                ro.addRequestListener(constructor.newInstance(args));

            }
            catch (ClassNotFoundException cnfe) {
                log.error("Error while loading RequestListeners, required class could not be loaded: "
                        + cnfe.toString());
                throw new OwsExceptionReport(cnfe.getMessage(), cnfe.getCause());
            }
            catch (NoSuchMethodException nsme) {
                log.error("Error while loading RequestListeners," + " no required constructor available: "
                        + nsme.toString());
                throw new OwsExceptionReport(nsme.getMessage(), nsme.getCause());
            }
            catch (InvocationTargetException ite) {
                log.error("The instatiation of a RequestListener failed: " + ite.toString());
                throw new OwsExceptionReport(ite.getMessage(), ite.getCause());
            }
            catch (InstantiationException ie) {
                log.error("The instatiation of a RequestListener failed: " + ie.toString());
                throw new OwsExceptionReport(ie.getMessage(), ie.getCause());
            }
            catch (IllegalAccessException iace) {
                log.error("The instatiation of a RequestListener failed: " + iace.toString());
                throw new OwsExceptionReport(iace.getMessage(), iace.getCause());
            }
        }

        return ro;
    }

    /**
     * Uses a thread for a delayed execution. This is necessary if both the catalog and the SIR run in the
     * same container. The update can be blocked if the {@link ICatalogStatusHandler} is not available in the
     * context.
     */
    private void startCatalogConnections() {
        if (log.isDebugEnabled())
            log.debug(" ***** Starting Thread for catalog connections with a delay of "
                    + STARTUP_CATALOG_CONNECTION_DELAY_SECS + " seconds ***** ");

        this.exec.submit(new Thread("CatalogConnector") {

            @Override
            public void run() {
                // wait with the catalog connection, because if the catalog runs
                // in the same tomcat, problems
                // might occur during startup phase

                try {
                    sleep(STARTUP_CATALOG_CONNECTION_DELAY_SECS * 1000);
                }
                catch (InterruptedException e1) {
                    log.error("Error sleeping before catalog connections.", e1);
                }

                log.info(" ***** Starting catalog connections ***** ");

                // run tasks for existing catalogs
                int i = 0;
                try {
                    IConnectToCatalogDAO catalogDao = getFactory().connectToCatalogDAO();
                    List<ICatalogConnection> savedConnections = catalogDao.getCatalogConnectionList();

                    IJobScheduler scheduler = SirConfigurator.getInstance().getJobSchedulerFactory().getJobScheduler();
                    for (ICatalogConnection iCatalogConnection : savedConnections) {
                        if (iCatalogConnection.getPushIntervalSeconds() != ICatalogConnection.NO_PUSH_INTERVAL) {
                            scheduler.submit(iCatalogConnection);
                            i++;
                        }
                        else {
                            if (log.isDebugEnabled())
                                log.debug("ICatalogConnection without push interval is ignored: "
                                        + iCatalogConnection.getConnectionID());
                        }
                    }
                }
                catch (OwsExceptionReport e) {
                    log.error("Could not run tasks for saved catalog connections.", e.getCause());
                }

                log.info(" ***** Scheduled " + i + " task(s) from the database. ***** ");
            }

        });
    }

    private ArrayList<String> loadListeners() throws OwsExceptionReport {

        ArrayList<String> listeners = new ArrayList<String>();
        String listenersList = this.props.getProperty(LISTENERS);

        if (listenersList == null) {
            log.error("No RequestListeners are defined in the ConfigFile!");
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionReport.ExceptionCode.NoApplicableCode,
                                 "SirConfigurator.loadListeners()",
                                 "No request listeners are defined in the config file!");
            throw se;
        }
        StringTokenizer tokenizer = new StringTokenizer(listenersList, ",");
        while (tokenizer.hasMoreTokens()) {
            listeners.add(tokenizer.nextToken());
        }
        return listeners;
    }

    /**
     * @return Returns the instance of the SirConfigurator. Null will be returned if the parameterized
     *         getInstance method was not invoked before. Usuallex this will be done in the SIR
     */
    public static SirConfigurator getInstance() {
        return instance;
    }

    public void newUpdateSequence() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(this.gmlDateFormat);
        this.updateSequence = dateFormat.format(new Date());
    }

    /**
     * @return the characterEncoding
     */
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    /**
     * @return the httpPostDecoder
     */
    public IHttpPostRequestDecoder getHttpPostDecoder() {
        return this.httpPostDecoder;
    }

    /**
     * @return the httpGetDecoder
     */
    public IHttpGetRequestDecoder getHttpGetDecoder() {
        return this.httpGetDecoder;
    }

    /**
     * @return the DaoFactory
     */
    public IDAOFactory getFactory() {
        return this.factory;
    }

    /**
     * @return the jobSchedulerFactory
     */
    public IJobSchedulerFactory getJobSchedulerFactory() {
        return this.jobSchedulerFactory;
    }

    /**
     * @return the transformerFactory
     */
    public ITransformerFactory getTransformerFactory() {
        return this.transformerFactory;
    }

    /**
     * @return the validatorFactory
     */
    public IValidatorFactory getValidatorFactory() {
        return this.validatorFactory;
    }

    /**
     * 
     * @return the status handler for external access (not from within this SIR instance)
     */
    public ICatalogStatusHandler getCatalogStatusHandler() {
        return this.catalogStatusHandler;
    }

    /**
     * 
     * Creates an CatalogFactory (an instance of the class provided in the sir.config file) for the service
     * located at the given URL.
     * 
     * @param url
     * @return
     * @throws OwsExceptionReport
     */
    public ICatalogFactory getCatalogFactory(URL url) throws OwsExceptionReport {
        try {
            return this.catalogFactoryConstructor.newInstance(url,
                                                              this.catalogInitClassificationFiles,
                                                              this.catalogInitSlotFile,
                                                              Boolean.valueOf(this.doNotCheckCatalogsList.contains(url)));
        }
        catch (Exception e) {
            log.error("The instatiation of a catalog factory failed." + e.toString());
            throw new OwsExceptionReport("The instatiation of a catalog factory failed: " + e.getMessage(),
                                         e.getCause());
        }
    }

    /**
     * @return the capabilitiesSkeleton
     */
    public CapabilitiesDocument getCapabilitiesSkeleton() {
        return this.capabilitiesSkeleton;
    }

    /**
     * @return the serviceVersion
     */
    public String getServiceVersion() {
        return this.serviceVersion;
    }

    /**
     * @return the updateSequence
     */
    public String getUpdateSequence() {
        return this.updateSequence;
    }

    /**
     * @return the gmlDateFormat
     */
    public String getGmlDateFormat() {
        return this.gmlDateFormat;
    }

    /**
     * @return the serviceUrl of the sir.config in the form "host:port/path"
     */
    public URL getServiceUrl() {
        return this.serviceUrl;
    }

    /**
     * @return the schemaUrl
     */
    public String getSchemaUrl() {
        return this.schemaUrl;
    }

    /**
     * @return the namespaceUri
     */
    public String getNamespaceUri() {
        return this.namespaceUri;
    }

    /**
     * @return the namespacePrefix
     */
    public String getNamespacePrefix() {
        return this.namespacePrefix;
    }

    /**
     * @return the testRequestPath
     */
    public String getTestRequestPath() {
        return this.testRequestPath;
    }

    /**
     * @return the profile4Discovery
     */
    public String getProfile4Discovery() {
        return this.profile4Discovery;
    }

    /**
     * @return the svrlSchema
     */
    public String getSvrlSchema() {
        return this.svrlSchema;
    }

    /**
     * 
     * @return
     */
    public String getProfile4DiscoveryDownloadPath() {
        return this.props.getProperty(PROFILE4DISCOVERY);
    }

    /**
     * 
     * @return
     */
    public String getSchemaDownloadLink() {
        return this.props.getProperty(SCHEMATRON_DOWNLOAD);
    }

    /**
     * @return the extendedDebugToConsole
     */
    public boolean isExtendedDebugToConsole() {
        return this.extendedDebugToConsole;
    }

    /**
     * does the translation from String representation of version number (to be optained by
     * getServiceVersion()) to enum of schema.
     * 
     * @return
     */
    public Enum getServiceVersionEnum() {
        String sv = getServiceVersion();

        if (sv.equals(SirConstants.SERVICE_VERSION_0_3_0))
            return VersionAttribute.Version.X_0_3_0;

        if (sv.equals(SirConstants.SERVICE_VERSION_0_3_1))
            return VersionAttribute.Version.X_0_3_1;

        throw new RuntimeException("Not a supported version!");
    }

    /**
     * @return the acceptedVersions
     */
    public String[] getAcceptedServiceVersions() {
        return this.acceptedVersions;
    }

    /**
     * 
     * @return
     */
    public ExecutorService getExecutor() {
        return this.exec;
    }

    /**
     * @return the validateRequests
     */
    public boolean isValidateRequests() {
        return this.validateRequests;
    }

    /**
     * @return the validateResponses
     */
    public boolean isValidateResponses() {
        return this.validateResponses;
    }

}
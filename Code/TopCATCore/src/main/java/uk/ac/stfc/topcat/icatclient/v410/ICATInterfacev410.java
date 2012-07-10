/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.stfc.topcat.icatclient.v410;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.xml.namespace.QName;
import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.exception.ICATMethodNotFoundException;
import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.core.gwt.module.TDatafile;
import uk.ac.stfc.topcat.core.gwt.module.TDatafileParameter;
import uk.ac.stfc.topcat.core.gwt.module.TDataset;
import uk.ac.stfc.topcat.core.gwt.module.TDatasetParameter;
import uk.ac.stfc.topcat.core.gwt.module.TFacilityCycle;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigator;
import uk.ac.stfc.topcat.core.gwt.module.TPublication;
import uk.ac.stfc.topcat.core.gwt.module.TShift;
import uk.ac.stfc.topcat.core.icat.ICATWebInterfaceBase;

/**
 * 
 */
public class ICATInterfacev410 extends ICATWebInterfaceBase {
    private ICAT service;
    private String serverName;

    public ICATInterfacev410(String serverURL, String serverName) throws MalformedURLException {
        service = new ICATService(new URL(serverURL), new QName("http://icatproject.org", "ICATService")).getICATPort();
        this.serverName = serverName;
    }

    public String loginLifetime(String username, String password, int hours) throws AuthenticationException {
        String result = new String();
        try {
            // TODO no longer uses hours
            result = service.login(username, password);
        } catch (IcatException_Exception ex) {
            // TODO check type
            throw new AuthenticationException("ICAT Server not available");
        } catch (javax.xml.ws.WebServiceException ex) {
            throw new AuthenticationException("ICAT Server not available");
        }
        return result;
    }

    public void logout(String sessionId) throws AuthenticationException {
        try {
            service.logout(sessionId);
        } catch (IcatException_Exception e) {
            // TODO check type
            throw new AuthenticationException("ICAT Server not available");
        } catch (javax.xml.ws.WebServiceException ex) {
            throw new AuthenticationException("ICAT Server not available");
        }
    }

    public Boolean isSessionValid(String sessionId) {
        try {
            return new Boolean(service.getRemainingMinutes(sessionId) > 0);
        } catch (javax.xml.ws.WebServiceException ex) {
        } catch (IcatException_Exception e) {
            IcatException ue = e.getFaultInfo();
            if (ue.getType().equals(IcatExceptionType.SESSION)) {
                return Boolean.FALSE;
            } else {
                // TODO handle other types
            }
        }
        return Boolean.FALSE;
    }

    public String getUserSurname(String sessionId, String userId) {
        String name;
        try {
            name = service.getUserName(sessionId);
        } catch (IcatException_Exception e) {
            return userId;
        }
        return name;
    }

    public ArrayList<String> listInstruments(String sessionId) {
        return searchList(sessionId, "DISTINCT Instrument.name");
    }

    public ArrayList<String> listInvestigationTypes(String sessionId) {
        return searchList(sessionId, "DISTINCT InvestigationType.name");
    }

    public ArrayList<TFacilityCycle> listFacilityCycles(String sessionId) throws ICATMethodNotFoundException {
        ArrayList<TFacilityCycle> facilityCycles = new ArrayList<TFacilityCycle>();
        // try {
        // // Get the ICAT webservice client and call get investigation types
        // List<FacilityCycle> fcList = service.listFacilityCycles(sessionId);
        // for (FacilityCycle fc : fcList) {
        // Date start = new Date();
        // Date end = new Date();
        // if (fc.getStartDate() != null)
        // start = fc.getStartDate().toGregorianCalendar().getTime();
        // if (fc.getFinishDate() != null)
        // end = fc.getFinishDate().toGregorianCalendar().getTime();
        // facilityCycles.add(new TFacilityCycle(fc.getDescription(),
        // fc.getName(), start, end));
        // }
        // } catch (IcatException_Exception ex) {
        // } catch (java.lang.NullPointerException ex) {
        // } catch (Exception ex) {
        // throw new ICATMethodNotFoundException(ex.getMessage());
        // }
        return facilityCycles;
    }

    public ArrayList<TInvestigation> getMyInvestigations(String sessionId) {
        ArrayList<TInvestigation> investigationList = new ArrayList<TInvestigation>();
        try {
            List<Object> resultInv = service.search(sessionId, "Investigation");
            for (Object inv : resultInv) {
                investigationList.add(copyInvestigationToTInvestigation(serverName, (Investigation) inv));
            }
        } catch (IcatException_Exception ex) {
            // TODO check type
        }
        Collections.sort(investigationList);
        return investigationList;
    }

    public ArrayList<TInvestigation> getMyInvestigationsIncludesPagination(String sessionId, int start, int end) {
        ArrayList<TInvestigation> investigationList = new ArrayList<TInvestigation>();
        try {
            List<Object> resultInv = service.search(sessionId, start + ", " + end + " Investigation");
            for (Object inv : resultInv) {
                investigationList.add(copyInvestigationToTInvestigation(serverName, (Investigation) inv));
            }
        } catch (IcatException_Exception ex) {
            // TODO check type
        }
        Collections.sort(investigationList);
        return investigationList;
    }

    public TInvestigation getInvestigationDetails(String sessionId, Long investigationId)
            throws AuthenticationException {
        TInvestigation ti = new TInvestigation();
        try {
            Investigation resultInv = (Investigation) service
                    .get(sessionId,
                            "Investigation INCLUDE Publication, InvestigationUser, Instrument, User, Shift, InvestigationParameter, ParameterType",
                            investigationId);
            ti = copyInvestigationToTInvestigation(serverName, resultInv);

            if (resultInv.getInstrument() != null) {
                ti.setInstrument(resultInv.getInstrument().getFullName());
            }
            ti.setProposal(resultInv.getSummary());
            ArrayList<TPublication> publicationList = new ArrayList<TPublication>();
            List<Publication> pubs = resultInv.getPublications();
            for (Publication pub : pubs) {
                publicationList.add(copyPublicationToTPublication(pub));
            }
            ti.setPublications(publicationList);

            ArrayList<TInvestigator> investigatorList = new ArrayList<TInvestigator>();
            List<InvestigationUser> users = resultInv.getInvestigationUsers();
            for (InvestigationUser user : users) {
                investigatorList.add(copyInvestigatorToTInvestigator(user));
            }
            Collections.sort(investigatorList);
            ti.setInvestigators(investigatorList);

            ArrayList<TShift> shiftList = new ArrayList<TShift>();
            List<Shift> shifts = resultInv.getShifts();
            for (Shift shift : shifts) {
                shiftList.add(copyShiftToTShift(shift));
            }
            ti.setShifts(shiftList);

            List<InvestigationParameter> params = resultInv.getParameters();
            // TODO currently single parameter need to make into list
            for (InvestigationParameter param : params) {
                ti.setParamName(param.getType().getName());
                if (param.getType().getValueType() == ParameterValueType.NUMERIC) {
                    ti.setParamValue(param.getNumericValue().toString());
                } else if (param.getType().getValueType() == ParameterValueType.STRING) {
                    ti.setParamValue(param.getStringValue());
                } else if (param.getType().getValueType() == ParameterValueType.DATE_AND_TIME) {
                    ti.setParamValue(param.getDateTimeValue().toGregorianCalendar().getTime().toString());
                }
            }
        } catch (IcatException_Exception ex) {
            // TODO check type
            throw new AuthenticationException(ex.getMessage());
        }
        return ti;
    }

    public ArrayList<TInvestigation> searchByAdvancedPagination(String sessionId, TAdvancedSearchDetails details,
            int start, int end) {
        ArrayList<TInvestigation> investigationList = new ArrayList<TInvestigation>();
        String query = getAdvancedQuery(details);
        List<Object> resultInv = null;
        try {
            resultInv = service.search(sessionId, start + ", " + end + query);
        } catch (IcatException_Exception ex) {
            // TODO check type
            System.out.println("ERROR - searchByAdvancedPagination: " + ex.getMessage());
        }

        for (Object inv : resultInv) {
            investigationList.add(copyInvestigationToTInvestigation(serverName, (Investigation) inv));
        }

        Collections.sort(investigationList);
        return investigationList;
    }

    public ArrayList<TDataset> getDatasetsInInvestigation(String sessionId, Long investigationId) {
        ArrayList<TDataset> datasetList = new ArrayList<TDataset>();
        try {
            Investigation resultInv = (Investigation) service.get(sessionId,
                    "Investigation INCLUDE Dataset, DatasetType", investigationId);
            List<Dataset> dList = resultInv.getDatasets();
            for (Dataset dataset : dList) {
                datasetList.add(new TDataset(serverName, dataset.getId().toString(), dataset.getName(), dataset
                        .getDescription(), dataset.getType().getName(), Boolean.toString(dataset.isComplete())));
            }
        } catch (IcatException_Exception ex) {
            // TODO check type
            System.out.println("ERROR - getDatasetsInInvestigation: " + ex.getMessage());
        }
        return datasetList;
    }

    public ArrayList<TDatasetParameter> getParametersInDataset(String sessionId, Long datasetId) {
        ArrayList<TDatasetParameter> result = new ArrayList<TDatasetParameter>();
        try {
            Dataset ds = (Dataset) service.get(sessionId, "Dataset INCLUDE DatasetParameter, ParameterType", datasetId);
            List<DatasetParameter> dsList = ds.getParameters();
            for (DatasetParameter dsParam : dsList) {
                System.out.println("parameter type: " + dsParam.getType());
                if (dsParam.getType().getValueType() == ParameterValueType.NUMERIC) {
                    result.add(new TDatasetParameter(dsParam.getType().getName(), dsParam.getType().getUnits(), dsParam
                            .getNumericValue().toString()));
                } else if (dsParam.getType().getValueType() == ParameterValueType.STRING) {
                    result.add(new TDatasetParameter(dsParam.getType().getName(), dsParam.getType().getUnits(), dsParam
                            .getStringValue()));
                } else if (dsParam.getType().getValueType() == ParameterValueType.DATE_AND_TIME) {
                    result.add(new TDatasetParameter(dsParam.getType().getName(), dsParam.getType().getUnits(), dsParam
                            .getDateTimeValue().toString()));
                }
            }
        } catch (IcatException_Exception e) {
            // TODO check type
            System.out.println("ERROR - getParametersInDataset: " + e.getMessage());
        }
        return result;
    }

    public ArrayList<TDatafile> getDatafilesInDataset(String sessionId, Long datasetId) {
        ArrayList<TDatafile> datafileList = new ArrayList<TDatafile>();
        try {
            Dataset resultInv = (Dataset) service.get(sessionId, "Dataset INCLUDE Datafile, DatafileFormat", datasetId);
            List<Datafile> dList = resultInv.getDatafiles();
            for (Datafile datafile : dList) {
                datafileList.add(copyDatafileToTDatafile(serverName, datafile));
            }
        } catch (IcatException_Exception ex) {
            // TODO check type
            System.out.println("ERROR - getDatafilesInDataset: " + ex.getMessage());
        }
        return datafileList;
    }

    public ArrayList<TDatafileParameter> getParametersInDatafile(String sessionId, Long datafileId) {
        ArrayList<TDatafileParameter> result = new ArrayList<TDatafileParameter>();
        try {
            Datafile df = (Datafile) service.get(sessionId, "Datafile INCLUDE DatasetParameter, ParameterType",
                    datafileId);
            List<DatafileParameter> dfList = df.getParameters();
            for (DatafileParameter dfParam : dfList) {
                if (dfParam.getType().getValueType() == ParameterValueType.NUMERIC) {
                    result.add(new TDatafileParameter(dfParam.getType().getName(), dfParam.getType().getUnits(),
                            dfParam.getNumericValue().toString()));
                } else if (dfParam.getType().getValueType() == ParameterValueType.STRING) {
                    result.add(new TDatafileParameter(dfParam.getType().getName(), dfParam.getType().getUnits(),
                            dfParam.getStringValue()));
                } else if (dfParam.getType().getValueType() == ParameterValueType.DATE_AND_TIME) {
                    result.add(new TDatafileParameter(dfParam.getType().getName(), dfParam.getType().getUnits(),
                            dfParam.getDateTimeValue().toString()));
                }
            }
        } catch (IcatException_Exception ex) {
            // TODO check type
        }
        return result;
    }

    public String downloadDatafiles(String sessionId, ArrayList<Long> datafileIds) {
        String result = "";
        // try {
        // result = service.downloadDatafiles(sessionId, datafileIds);
        // } catch (IcatException_Exception ex) {
        // // TODO check type
        // }
        return result;
    }

    public String downloadDataset(String sessionId, Long datasetId) {
        String result = "";
        // try {
        // result = service.downloadDataset(sessionId, datasetId);
        // } catch (IcatException_Exception ex) {
        // // TODO check type
        // }
        return result;
    }

    public ArrayList<String> getKeywordsForUser(String sessionId) {
        return searchList(sessionId, "DISTINCT Keyword.name");
    }

    public ArrayList<String> getKeywordsInInvestigation(String sessionId, Long investigationId) {
        ArrayList<String> keywords = new ArrayList<String>();
        try {
            Investigation inv = (Investigation) service
                    .get(sessionId, "Investigation INCLUDE Keyword", investigationId);
            List<Keyword> resultKeywords = inv.getKeywords();
            for (Keyword key : resultKeywords) {
                keywords.add(key.getName());
            }
        } catch (IcatException_Exception ex) {
            // TODO check type
        }
        return keywords;
    }

    public ArrayList<TInvestigation> searchByKeywords(String sessionId, ArrayList<String> keywords) {
        // call the search using keyword method
        List<Object> resultInvestigations = null;
        ArrayList<TInvestigation> returnTInvestigations = new ArrayList<TInvestigation>();
        String query = "DISTINCT Investigation <-> Keyword[name IN " + getIN(keywords) + "]";
        try {
            resultInvestigations = service.search(sessionId, query);
            for (Object inv : resultInvestigations) {
                returnTInvestigations.add(copyInvestigationToTInvestigation(serverName, (Investigation) inv));
            }
        } catch (IcatException_Exception ex) {
            // TODO check type
        }
        Collections.sort(returnTInvestigations);
        return returnTInvestigations;
    }

    public ArrayList<TDatafile> searchByRunNumber(String sessionId, ArrayList<String> instruments,
            float startRunNumber, float endRunNumber) {
        List<Object> resultDatafiles = null;
        ArrayList<TDatafile> returnTDatafiles = new ArrayList<TDatafile>();
        try {
            resultDatafiles = service.search(sessionId,
                    "DISTINCT Datafile  INCLUDE DatafileFormat <-> Dataset <-> Investigation <-> Instrument[name IN "
                            + getIN(instruments)
                            + "] <-> DatafileParameter[type.name='run_number' AND numericValue BETWEEN "
                            + startRunNumber + " AND " + endRunNumber + "]");
        } catch (IcatException_Exception ex) {
            // TODO check type
            System.out.println("ERROR - searchByRunNumber: " + ex.getMessage());
        }
        for (Object datafile : resultDatafiles) {
            returnTDatafiles.add(copyDatafileToTDatafile(serverName, (Datafile) datafile));
        }
        return returnTDatafiles;
    }

    public ArrayList<String> getKeywordsForUserWithStartMax(String sessionId, String partialKey, int numberOfKeywords) {

        ArrayList<String> resultKeywords = new ArrayList<String>();
        try {
            List<Object> results = service.search(sessionId, "0," + numberOfKeywords + "DISTINCT Keyword.name LIKE "
                    + partialKey + "%");
            for (Object keyword : results) {
                resultKeywords.add((String) keyword);
            }
        } catch (IcatException_Exception ex) {
            // TODO check type
        }
        return resultKeywords;
    }

    private String getAdvancedQuery(TAdvancedSearchDetails details) {
        StringBuilder query = new StringBuilder(" DISTINCT Investigation");
        boolean addAnd = false;

        // Dates
        if (details.getStartDate() != null && details.getEndDate() != null) {
            addAnd = true;
            String startDate = getDate(details.getStartDate());
            String endDate = getDate(details.getEndDate());
            query.append(" [((startDate<=" + startDate + " AND endDate>=" + startDate + ") OR (startDate>=" + startDate
                    + " AND endDate<=" + endDate + ") OR (startDate<=" + endDate + " AND endDate>=" + endDate + "))");
        }

        // Proposal Abstract
        if (details.getProposalAbstract() != null) {
            if (addAnd) {
                query.append(" AND");
            } else {
                query.append(" [");
                addAnd = true;
            }
            query.append(" summary='" + details.getProposalAbstract() + "'");
        }

        // Proposal Title
        if (details.getPropostaltitle() != null) {
            if (addAnd) {
                query.append(" AND");
            } else {
                query.append(" [");
                addAnd = true;
            }
            query.append(" title='" + details.getPropostaltitle() + "'");
        }

        if (addAnd) {
            query.append("]");
        }

        // Data File Name
        if (details.getDatafileName() != null) {
            query.append(" <-> Dataset <-> Datafile[name='" + details.getDatafileName() + "']");
        }

        // Grant Id
        if (details.getGrantId() != null) {
            query.append(" <-> InvestigationParameter [type.name='grant_id' AND stringValue='" + details.getGrantId()
                    + "']");
        }

        // Instrument
        if (details.getInstrumentList().size() > 0) {
            query.append(" <-> Instrument[name IN " + getIN(details.getInstrumentList()) + "]");
        }

        // Investigation Type
        if (details.getInvestigationTypeList().size() > 0) {
            query.append(" <-> InvestigationType[name IN " + getIN(details.getInvestigationTypeList()) + "]");
        }

        // Investigator Name
        if (details.getInvestigatorNameList().size() > 0) {
            query.append(" <-> InvestigationUser <-> User[name IN " + getIN(details.getInvestigatorNameList()) + "]");
        }

        // Keywords
        if (details.getKeywords().size() > 0
                && !(details.getKeywords().size() == 1 && details.getKeywords().get(0).length() == 0)) {
            query.append(" <-> Keyword[name IN " + getIN(details.getKeywords()) + "]");
        }

        // Run Numbers
        if (details.getRbNumberStart() != null && details.getRbNumberEnd() != null) {
            query.append(" <-> InvestigationParameter [type.name='run_number_range' AND ((rangeBottom<="
                    + details.getRbNumberStart() + " AND rangeTop>=" + details.getRbNumberStart()
                    + ") OR (rangeBottom>=" + details.getRbNumberStart() + " AND rangeTop<=" + details.getRbNumberEnd()
                    + ") OR (rangeBottom<=" + details.getRbNumberEnd() + " AND rangeTop>=" + details.getRbNumberEnd()
                    + "))]");
        }

        // Sample
        if (details.getSample() != null) {
            query.append(" <-> Sample[name='" + details.getSample() + "']");
        }

        System.out.println("INFO - searchByAdvancedPagination: " + query.toString());
        return query.toString();
    }

    private String getDate(Date date) {
        StringBuilder retDate = new StringBuilder();
        retDate.append("{ts ");
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        retDate.append(formater.format(date));
        retDate.append("}");
        return retDate.toString();
    }

    private TInvestigation copyInvestigationToTInvestigation(String serverName, Investigation inv) {
        String id = inv.getId().toString();
        Date invStartDate = null;
        Date invEndDate = null;

        if (inv.getStartDate() != null) {
            invStartDate = inv.getStartDate().toGregorianCalendar().getTime();
        }
        if (inv.getEndDate() != null) {
            invEndDate = inv.getEndDate().toGregorianCalendar().getTime();
        }
        return new TInvestigation(id, inv.getName(), serverName, inv.getTitle(), invStartDate, invEndDate,
                inv.getVisitId());
    }

    private TDatafile copyDatafileToTDatafile(String serverName, Datafile datafile) {
        String format = "";
        String formatVersion = "";
        String formatType = "";
        Date createDate = null;
        if (datafile.getDatafileFormat() != null) {
            format = datafile.getDatafileFormat().getName();
            formatVersion = datafile.getDatafileFormat().getVersion();
            formatType = datafile.getDatafileFormat().getType();
        }
        if (datafile.getDatafileCreateTime() != null) {
            createDate = datafile.getDatafileCreateTime().toGregorianCalendar().getTime();
        }
        return new TDatafile(serverName, datafile.getId().toString(), datafile.getName(), Integer.getInteger(datafile
                .getFileSize().toString()), format, formatVersion, formatType, createDate, datafile.getLocation());
    }

    private TPublication copyPublicationToTPublication(Publication pub) {
        return new TPublication(pub.getFullReference(), pub.getId(), pub.getRepository(), pub.getRepositoryId(),
                pub.getUrl());
    }

    private TInvestigator copyInvestigatorToTInvestigator(InvestigationUser investigator) {
        return new TInvestigator("", "", investigator.getUser().getFullName(), investigator.getRole());
    }

    private TShift copyShiftToTShift(Shift shift) {
        return new TShift(shift.getComment(), shift.getStartDate().toGregorianCalendar().getTime(), shift.getEndDate()
                .toGregorianCalendar().getTime());
    }

    public ArrayList<String> searchList(String sessionId, String query) {
        ArrayList<String> returnList = new ArrayList<String>();
        try {
            List<Object> results = service.search(sessionId, query);
            for (Object item : results) {
                returnList.add((String) item);
            }
        } catch (java.lang.NullPointerException ex) {
        } catch (IcatException_Exception ex) {
            // TODO check type
        }
        return returnList;
    }

    private String getIN(List<String> ele) {
        final StringBuilder infield = new StringBuilder("(");
        for (final String t : ele) {
            if (infield.length() != 1) {
                infield.append(',');
            }
            infield.append('\'').append(t).append('\'');
        }
        infield.append(')');
        return infield.toString();
    }
}
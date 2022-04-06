package org.swellrt.server.box.servlet;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.EmailValidator;
import org.waveprotocol.box.server.account.AccountData;
import org.waveprotocol.box.server.account.HumanAccountData;
import org.waveprotocol.box.server.account.HumanAccountDataImpl;
import org.waveprotocol.box.server.authentication.PasswordDigest;
import org.waveprotocol.box.server.authentication.SessionManager;
import org.waveprotocol.box.server.persistence.AccountAttachmentStore;
import org.waveprotocol.box.server.persistence.AccountAttachmentStore.Attachment;
import org.waveprotocol.box.server.persistence.AccountStore;
import org.waveprotocol.box.server.persistence.AttachmentUtil;
import org.waveprotocol.box.server.persistence.PersistenceException;
import org.waveprotocol.wave.model.wave.InvalidParticipantAddress;
import org.waveprotocol.wave.model.wave.ParticipantId;
import org.waveprotocol.wave.util.logging.Log;

import com.google.gson.JsonParseException;
import com.google.inject.Inject;
import com.typesafe.config.Config;

/**
 * Service for creating and editing accounts.
 *
 * @author pablojan@gmail.com (Pablo Ojanguren)
 *
 * Create new account
 *
 * POST /account { id : <ParticipantId>, password : <String>, ... }
 *
 *
 * Edit account profile (empty values are deleted)
 *
 * POST /account/{ParticipantId.name} { ... }
 *
 *
 * Get account profile
 *
 * GET /account/{ParticipantId.name}
 *
 *
 * Get multiple account profiles
 *
 * GET /account?p=user1@domain;user2@domain
 */
@SuppressWarnings("deprecation")
public class AccountService extends BaseService {


  public static class AccountServiceData extends ServiceData {

    public String id;
    public String name;
    public String password;
    public String email;
    public String avatarData;
    public String avatarUrl;
    public String locale;
    public String sessionId;
    public String transientSessionId;
    public String domain;

    public AccountServiceData() {

    }

    public AccountServiceData(String id) {
      this.id = id;
    }

  }



  private static final Log LOG = Log.get(AccountService.class);

  private final AccountStore accountStore;

  private final AccountAttachmentStore attachmentAccountStore;

  private final String domain;

  @Inject
  public AccountService(SessionManager sessionManager, AccountStore accountStore,
      AccountAttachmentStore attachmentAccountStore,
      Config config) {
	  this(sessionManager, accountStore, attachmentAccountStore, config.getString("core.wave_server_domain"));
  }

  protected AccountService(SessionManager sessionManager, AccountStore accountStore,
      AccountAttachmentStore attachmentAccountStore, String waveDomain) {
	    super(sessionManager);
	    this.accountStore = accountStore;
	    this.attachmentAccountStore = attachmentAccountStore;
	    this.domain = waveDomain;
  }

  protected ParticipantId getParticipantFromRequest(HttpServletRequest req)
      throws InvalidParticipantAddress {

    String[] pathTokens = SwellRtServlet.getCleanPathInfo(req).split("/");
    String participantToken = pathTokens.length > 2 ? pathTokens[2] : null;

    if (participantToken == null) throw new InvalidParticipantAddress("null", "Address is null");

    ParticipantId participantId =
        participantToken.contains("@") ? ParticipantId.of(participantToken) : ParticipantId
            .of(participantToken + "@" + domain);

    return participantId;

  }

  protected String getAvatarFileFromRequest(HttpServletRequest req)
      throws InvalidParticipantAddress {

    String[] pathTokens = SwellRtServlet.getCleanPathInfo(req).split("/");
    String avatarFileName = pathTokens.length > 4 ? pathTokens[4] : null;

    return avatarFileName;
  }

  protected Collection<ParticipantId> getParticipantsFromRequestQuery(HttpServletRequest req) {
    try {
      String query = URLDecoder.decode(req.getParameter("p"), "UTF-8");
      String[] participantAddresses = query.split(";");

      List<ParticipantId> participantIds= new ArrayList<ParticipantId>();

      for (String address : participantAddresses) {
        try {
          participantIds.add(ParticipantId.ofUnsafe(address));
        } catch (IllegalArgumentException e) {
          // Ignore
        }
      }

      return participantIds;

    } catch (Exception e) {
      return null;
    }

  }

  protected void createAccount(HttpServletRequest req, HttpServletResponse response)
      throws ServiceException, IOException {

    UrlBuilder urlBuilder = ServiceUtils.getUrlBuilder(req);

    try {

      AccountServiceData userData = getRequestServiceData(req);

      if (userData.id == null) {
        sendResponseError(response, HttpServletResponse.SC_BAD_REQUEST, RC_MISSING_PARAMETER);
        return;
      }

      ParticipantId participantId =
          userData.id.contains("@") ? ParticipantId.of(userData.id) : ParticipantId.of(userData.id
              + "@" + domain);

      AccountData accountData = accountStore.getAccount(participantId);


      if (accountData != null) {
        sendResponseError(response, HttpServletResponse.SC_FORBIDDEN, RC_ACCOUNT_ALREADY_EXISTS);
        return;
      }

      if (userData.password == null) {
        userData.password = "";
      }

      HumanAccountDataImpl account =
          new HumanAccountDataImpl(participantId, new PasswordDigest(
              userData.password.toCharArray()));


      if (userData.email != null) {
        if (!EmailValidator.getInstance().isValid(userData.email)) {
          sendResponseError(response, HttpServletResponse.SC_BAD_REQUEST, RC_INVALID_EMAIL_ADDRESS);
          return;
        }

        account.setEmail(userData.email);
      }

      if (userData.locale != null) account.setLocale(userData.locale);
      else {
        account.setLocale(req.getLocale().toString());
      }

      if (userData.avatarData != null) {
        // Store avatar
        String avatarFileId = storeAvatar(participantId, userData.avatarData, null);
        account.setAvatarFileId(avatarFileId);
      }


      accountStore.putAccount(account);

      sendResponse(response, toServiceData(urlBuilder, account));
      return;


    } catch (PersistenceException e) {
      throw new ServiceException("Can't write account to storage", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, RC_INTERNAL_SERVER_ERROR ,e);
    } catch (JsonParseException e) {
      throw new ServiceException("Can't parse JSON", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, RC_INVALID_JSON_SYNTAX ,e);
    } catch (InvalidParticipantAddress e) {
      throw new ServiceException("Can't get participant from request" ,HttpServletResponse.SC_BAD_REQUEST, RC_INVALID_ACCOUNT_ID_SYNTAX, e);
    }


  }


  protected void updateAccount(HttpServletRequest req, HttpServletResponse response)
      throws ServiceException, IOException {

    // POST /account/joe update user's account

    ParticipantId loggedInUser = sessionManager.getLoggedInUser(req);

    try {

      ParticipantId participantId = getParticipantFromRequest(req);

      // if the account exists, only the user can modify the profile
      if (!participantId.equals(loggedInUser)) {
        sendResponseError(response, HttpServletResponse.SC_FORBIDDEN, RC_ACCOUNT_NOT_LOGGED_IN);
        return;
      }

      // Modify

      AccountServiceData userData = getRequestServiceData(req);

      if (participantId.isAnonymous()) {
        updateAccountInSession(req, userData);
      } else {

        AccountData accountData = accountStore.getAccount(participantId);

        if (accountData == null) {
          sendResponseError(response, HttpServletResponse.SC_FORBIDDEN, RC_ACCOUNT_NOT_FOUND);
          return;
        }

        HumanAccountData account = accountData.asHuman();
        updateAccountInStore(participantId, userData, account);
      }

      // send updated response
      getParticipantAccount(req, response);

    } catch (PersistenceException e) {
      throw new ServiceException("Can't write account to storage", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, RC_INTERNAL_SERVER_ERROR ,e);
    } catch (JsonParseException e) {
      throw new ServiceException("Can't parse JSON", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, RC_INVALID_JSON_SYNTAX ,e);
    } catch (InvalidParticipantAddress e) {
      throw new ServiceException("Can't get participant from request" ,HttpServletResponse.SC_BAD_REQUEST, RC_INVALID_ACCOUNT_ID_SYNTAX, e);
    }

  }

  protected void updateAccountInSession(HttpServletRequest req, AccountServiceData receivedData) {

    Map<String, String> sessionProps = new HashMap<String, String>();

    if (receivedData.has("email") && !receivedData.email.isEmpty()) {
      sessionProps.put("email", receivedData.email);
    }

    if (receivedData.has("name") && !receivedData.name.isEmpty())  {
      sessionProps.put("name", receivedData.name);
    }

    sessionManager.setSessionProperties(req, sessionProps);

  }


  protected void updateAccountInStore(ParticipantId participantId, AccountServiceData receivedData, HumanAccountData account) throws ServiceException, IOException, PersistenceException {

    if (receivedData.has("email")) {
      try {
        if (receivedData.email.isEmpty())
          account.setEmail(null);
        else
          account.setEmail(receivedData.email);
      } catch (IllegalArgumentException e) {
        throw new ServiceException("Invalid email address", HttpServletResponse.SC_BAD_REQUEST, RC_INVALID_EMAIL_ADDRESS);
      }
    }

    if (receivedData.has("locale")) account.setLocale(receivedData.locale);


    if (receivedData.has("avatarData")) {
      if (receivedData.avatarData == null || receivedData.avatarData.isEmpty()
          || "data:".equals(receivedData.avatarData)) {
        // Delete avatar
        deleteAvatar(account.getAvatarFileId());
        account.setAvatarFileId(null);
      } else {
        String avatarFileId =
            storeAvatar(participantId, receivedData.avatarData, account.getAvatarFileName());
        account.setAvatarFileId(avatarFileId);
      }
    }

    if (receivedData.has("name")) account.setName(receivedData.name);

    accountStore.putAccount(account);

  }

  /**
   * Returns an avatar image from the provided participant
   *
   * @param avatarOwnerAddress
   * @param req
   * @param response
   * @throws IOException
   */
  protected void getAvatar(HttpServletRequest req, HttpServletResponse response) throws ServiceException, IOException {

    // GET /account/joe/avatar/[filename]
    try {

      ParticipantId avatarOwnerId = getParticipantFromRequest(req);

      // Retrieve the avatar's owner account data
      AccountData accountData = accountStore.getAccount(avatarOwnerId);

      if (accountData == null) {
        throw new ServiceException("Can't retrieve user account", HttpServletResponse.SC_NOT_FOUND, "ACCOUNT_NOT_FOUND");
      }

      String fileName = getAvatarFileFromRequest(req);

      if (fileName == null) {
        throw new ServiceException("Can't extract avatar file from request", HttpServletResponse.SC_BAD_REQUEST, "ATTACHMENT_FILE_NAME_ERROR");
      }

      Attachment avatar = attachmentAccountStore.getAvatar(fileName);

      if (avatar == null) {
        LOG.warning("Avatar file not found: " + fileName);
        throw new ServiceException("Avatar file not found", HttpServletResponse.SC_NOT_FOUND, "ACCOUNT_ATTACHMENT_NOT_FOUND");
      }


      response.setContentType(accountData.asHuman().getAvatarMimeType());
      response.setContentLength((int) avatar.getSize());
      response.setStatus(HttpServletResponse.SC_OK);
      response.setDateHeader("Last-Modified", Calendar.getInstance().getTimeInMillis());
      AttachmentUtil.writeTo(avatar.getInputStream(), response.getOutputStream());

    } catch (PersistenceException e) {
      throw new ServiceException("Can't read avatar from storage", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, RC_INTERNAL_SERVER_ERROR ,e);
    } catch (InvalidParticipantAddress e) {
      throw new ServiceException("Can't get participant from request" ,HttpServletResponse.SC_BAD_REQUEST, RC_INVALID_ACCOUNT_ID_SYNTAX, e);
    }

  }


  protected void getParticipantAccount(HttpServletRequest req, HttpServletResponse response)
      throws ServiceException, IOException {

    UrlBuilder urlBuilder = ServiceUtils.getUrlBuilder(req);

    try {

      ParticipantId loggedInUser = getLoggedInUser(req);

      ParticipantId participantId = getParticipantFromRequest(req);

      if (!participantId.equals(loggedInUser)) {

        // GET /account/joe retrieve user's account data only public fields

        AccountData accountData = accountStore.getAccount(participantId);
        if (accountData == null) {
          sendResponseError(response, HttpServletResponse.SC_NOT_FOUND, RC_ACCOUNT_NOT_FOUND);
        }
        sendResponse(response, toPublicServiceData(urlBuilder, accountData.asHuman()));

      } else {

        // GET /account/joe retrieve user's account data including private
        // fields

        if (participantId.isAnonymous()) {

          Map<String, String> properties = sessionManager.getSessionProperties(req);

          AccountServiceData data = new AccountServiceData();
          data.id = participantId.getAddress();
          data.email = properties.containsKey("email") ? properties.get("email") : null;
          data.name = properties.containsKey("name") && !properties.get("name").isEmpty() ? properties.get("name") : "Anonymous";

          sendResponse(response, data);

        } else {

          AccountData accountData = accountStore.getAccount(participantId);
          if (accountData == null) {
            sendResponseError(response, HttpServletResponse.SC_NOT_FOUND, RC_ACCOUNT_NOT_FOUND);
          }
          sendResponse(response, toServiceData(urlBuilder, accountData.asHuman()));
        }

        return;
      }

    } catch (PersistenceException e) {
      throw new ServiceException("Can't read account from storage", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, RC_INTERNAL_SERVER_ERROR ,e);
    } catch (InvalidParticipantAddress e) {
      throw new ServiceException("Can't get participant from request" ,HttpServletResponse.SC_BAD_REQUEST, RC_INVALID_ACCOUNT_ID_SYNTAX, e);
    }

  }


  protected void queryParticipantAccount(HttpServletRequest req, HttpServletResponse response)
      throws ServiceException, IOException {


    // GET /account?p=joe@local.net;tom@local.net

    // We require an open session, at least anonymous
    checkAnySession(req);

    Collection<ParticipantId> participantsQuery = getParticipantsFromRequestQuery(req);

    if (participantsQuery == null) {
      sendResponseError(response, HttpServletResponse.SC_BAD_REQUEST, RC_MISSING_PARAMETER);
      return;
    }

    sendResponse(response,
        toPublicServiceData(ServiceUtils.getUrlBuilder(req), participantsQuery, accountStore));


  }


  @Override
  public void execute(HttpServletRequest req, HttpServletResponse response) throws IOException {

    String[] pathTokens = SwellRtServlet.getCleanPathInfo(req).split("/");
    String participantToken = pathTokens.length > 2 ? pathTokens[2] : null;
    String participantOp = pathTokens.length > 3 ? pathTokens[3] : null;

    try {

      if (req.getMethod().equals("POST") && participantToken == null) {

        createAccount(req, response);

      } else if (req.getMethod().equals("POST") && participantToken != null) {

        updateAccount(req, response);

      } else if (req.getMethod().equals("GET")) {

        if (participantToken != null) {

          if (participantOp != null && participantOp.equals("avatar"))
            getAvatar(req, response);
          else
            getParticipantAccount(req, response);

        } else {

          queryParticipantAccount(req, response);

        }
      }

    } catch (ServiceException e) {
      sendResponseError(response, e.getHttpResponseCode(), e.getServiceResponseCode());
      return;
    } catch (IOException e) {
      sendResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, RC_INTERNAL_SERVER_ERROR);
      return;
    }

  }


  protected String storeAvatar(ParticipantId participantId, String avatarData,
      String currentAvatarFileName) throws IOException {

    if (!avatarData.startsWith("data:")) {
      throw new IOException("Avatar data syntax is not a valida RFC 2397 data URI");
    }

    // Store avatar first and get the storage's file name
    int dataUriSeparatorIndex = avatarData.indexOf(";");
    String mimeType = avatarData.substring("data:".length(), dataUriSeparatorIndex);
    // Remove the base64, prefix
    String base64Data = avatarData.substring(dataUriSeparatorIndex + 8, avatarData.length());

    return attachmentAccountStore.storeAvatar(participantId, mimeType, base64Data,
        currentAvatarFileName);

  }

  protected void deleteAvatar(String avatarFileId) {


  }

  protected AccountServiceData getRequestServiceData(HttpServletRequest request)
      throws JsonParseException, IOException {
    StringWriter writer = new StringWriter();
    IOUtils.copy(request.getInputStream(), writer, Charset.forName("UTF-8"));

    String json = writer.toString();

    if (json == null) throw new JsonParseException("Null JSON message");

    return (AccountServiceData) ServiceData.fromJson(json, AccountServiceData.class);
  }



  protected Collection<AccountServiceData> toPublicServiceData(UrlBuilder urlBuilder,
      Collection<ParticipantId> participants, AccountStore accountStore) {

    List<AccountServiceData> accountServiceDataList = new ArrayList<AccountServiceData>();

    for (ParticipantId p : participants) {

      try {
        AccountData accountData = accountStore.getAccount(p);

        if (accountData != null && accountData.isHuman()) {
          accountServiceDataList.add(toPublicServiceData(urlBuilder, accountData.asHuman()));
        } else {
          accountServiceDataList.add(new AccountServiceData(p.getAddress()));
        }

      } catch (PersistenceException e) {
        accountServiceDataList.add(new AccountServiceData(p.getAddress()));
      }

    }

    return accountServiceDataList;
  }



  protected static String getAvatarUrl(UrlBuilder urlBuilder, HumanAccountData account) {
    if (account.getAvatarFileName() == null) return null;

    return urlBuilder.build(
        "/account/" + account.getId().getName() + "/avatar/" + account.getAvatarFileName(), null);
  }

  protected static AccountServiceData toServiceData(UrlBuilder urlBuilder, HumanAccountData account) {

    AccountServiceData data = new AccountServiceData();

    data.id = account.getId().getAddress();
    data.email = account.getEmail() == null ? "" : account.getEmail();
    String avatarUrl = getAvatarUrl(urlBuilder, account);
    data.avatarUrl = avatarUrl == null ? "" : avatarUrl;
    data.locale = account.getLocale() == null ? "" : account.getLocale();
    data.name = account.getName() == null ? "" : account.getName();

    return data;
  }

  protected static AccountServiceData toPublicServiceData(UrlBuilder urlBuilder,
      HumanAccountData account) {

    AccountServiceData data = new AccountServiceData();

    data.id = account.getId().getAddress();
    String avatarUrl = getAvatarUrl(urlBuilder, account);
    data.avatarUrl = avatarUrl == null ? "" : avatarUrl;
    data.locale = account.getLocale() == null ? "" : account.getLocale();
    data.name = account.getName() == null ? "" : account.getName();

    return data;
  }


}

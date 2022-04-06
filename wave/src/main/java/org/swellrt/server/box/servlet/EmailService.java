package org.swellrt.server.box.servlet;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.NotImplementedException;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.ToolManager;
import org.waveprotocol.box.server.account.AccountData;
import org.waveprotocol.box.server.account.HumanAccountData;
import org.waveprotocol.box.server.authentication.SessionManager;
import org.waveprotocol.box.server.persistence.AccountStore;
import org.waveprotocol.box.server.persistence.PersistenceException;
import org.waveprotocol.wave.model.wave.ParticipantId;
import org.waveprotocol.wave.util.logging.Log;

import com.google.inject.Inject;
import com.typesafe.config.Config;

/**
 *
 * Set or update email address for the current logged in user
 * <p>
 *
 * <pre>
 * POST /email?method=set&email={emailAddress}
 * </pre>
 * <p>
 * <br>
 * Send a reset password email for a user identified by email address or id.
 * <p>
 *
 * <pre>
 * POST /email?method=password-reset&id-or-email={idOrEmail}&recover-url={recoverUrl}
 * </pre>
 *
 * <p>
 * recoverUrl parameter must contain a valid URL with variables "$token" and
 * "$user-id"
 * <p>
 * TODO: change service name to something more related to, e.g. POST password/recover, POST account/email
 * TODO: control exceptions on missing parameters
 */
public class EmailService extends BaseService {

  public static final String EMAIL = "email";

  public static final String ID_EMAIL = "id-or-email";

  private static final String METHOD = "method";

  private static final String SET = "set";

  private static final String PASSWORD_RESET = "password-reset";

  private static final String RECOVER_URL = "recover-url";

  /*
   * Path that has the default templates and translations inside the classpath
   */
  private static final String CLASSPATH_VELOCITY_PATH = "org/swellrt/server/velocity/";

  private static final String RECOVER_PASSWORD_TEMPLATE = "RecoverPassword.vm";

  private static final Log LOG = Log.get(EmailService.class);

  private static final String RECOVER_PASSWORD_BUNDLE = "EmailMessages";


  private final AccountStore accountStore;
  private final String host;
  private final String from;
  private final String recoverPasswordTemplateName;
  private final VelocityEngine ve;
  private URLClassLoader loader;
  private Session mailSession;
  private ToolManager manager;

  private String recoverPasswordMessages;

  private EmailSender emailSender;

  private DecoupledTemplates decTemplates;

  @Inject
  public EmailService(SessionManager sessionManager, AccountStore accountStore, Config config,
      EmailSender emailSender, DecoupledTemplates decTemplates) {
    super(sessionManager);
    this.accountStore = accountStore;
    String velocityPath = config.getString("email.template_path");
    this.host = config.getString("email.host");
    this.from = config.getString("email.from_email_address");
    this.emailSender = emailSender;
    this.decTemplates = decTemplates;

    Properties p = new Properties();
    p.put("resource.loader", "file, class");
    p.put("file.resource.loader.class",
        "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
    p.put("file.resource.loader.path", velocityPath);
    p.put("class.resource.loader.class",
        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

    ve = new VelocityEngine();

    ve.init(p);

    this.recoverPasswordTemplateName = ve.resourceExists("RecoverPassword.vm")
        ? RECOVER_PASSWORD_TEMPLATE : CLASSPATH_VELOCITY_PATH + RECOVER_PASSWORD_TEMPLATE;

    this.recoverPasswordMessages =
        new File(velocityPath + RECOVER_PASSWORD_BUNDLE + ".properties").exists()
            ? RECOVER_PASSWORD_BUNDLE
            : CLASSPATH_VELOCITY_PATH.replace("/", ".") + RECOVER_PASSWORD_BUNDLE;


    try {
      // based on http://stackoverflow.com/a/15654598/4928558
      File file = new File(velocityPath);
      URL[] urls = {file.toURI().toURL()};
      loader = new URLClassLoader(urls);
    } catch (MalformedURLException e) {
      LOG.warning("Error constructing classLoader for velocity internationalization resources:"
          + e.getMessage());
    }

    Properties properties = new Properties();


    // Get the default Session object.
    mailSession = Session.getDefaultInstance(properties, null);

    // Setup mail server
    properties.setProperty("mail.smtp.host", host);
    properties.setProperty("mail.smtp.from", from);

    manager = new ToolManager(false);

    manager.setVelocityEngine(ve);

    manager.configure("velocity-tools-config.xml");

  }


  @Override
  public void execute(HttpServletRequest req, HttpServletResponse response) throws IOException {


    Enumeration<String> paramNames = req.getParameterNames();

    if (!paramNames.hasMoreElements()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No parameters found!");
      return;
    } else {

      String method = req.getParameter(METHOD);
      String email = req.getParameter(EMAIL);

      switch (method) {

        case SET:
          HumanAccountData account = sessionManager.getLoggedInAccount(req).asHuman();

          if (account != null && account.getId().isAnonymous()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "User is anonymous");
            return;
          }

          try {

            account.setEmail(email);
            accountStore.putAccount(account);
            response.setStatus(HttpServletResponse.SC_OK);

          } catch (IllegalArgumentException t) {

            response.sendError(HttpServletResponse.SC_BAD_REQUEST, t.getMessage());

          } catch (PersistenceException e) {

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());

          }

          break;

        case PASSWORD_RESET:

          String recoverUrl = URLDecoder.decode(req.getParameter(RECOVER_URL), "UTF-8");
          String idOrEmail = URLDecoder.decode(req.getParameter(ID_EMAIL), "UTF-8");

          String emailAddress = "";
          try {

            List<AccountData> accounts = null;
            try {
              accounts = accountStore.getAccountByEmail(idOrEmail);
            } catch (NotImplementedException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

            // try to find by username if not found by email
            if (accounts == null || accounts.isEmpty()) {
              AccountData acc = accountStore.getAccount(new ParticipantId(idOrEmail));

              if (acc != null && !acc.getId().isAnonymous()) {
                accounts.add(acc);
                emailAddress = acc.asHuman().getEmail();
              }
            } else {
              emailAddress = idOrEmail;
            }

            if (accounts != null && !accounts.isEmpty()) {

              for (AccountData a : accounts) {

            	String userAddress = a.getId().getAddress();
              String userName = a.asHuman().getName() != null ? a.asHuman().getName()
                  : a.getId().getAddress().split("@")[0];

                double random = Math.random();

                String token =
                    Base64.encodeBase64URLSafeString((String.valueOf(random)).getBytes());

                a.asHuman().setRecoveryToken(token);
                accountStore.putAccount(a);

                String recoverUrlCp = recoverUrl;

                if (recoverUrlCp.contains("$user-id")) {
                  recoverUrlCp = recoverUrlCp.replaceAll("\\$user-id", userAddress);
                }

                if (recoverUrlCp.contains("$token")) {
                  recoverUrlCp = recoverUrlCp.replaceAll("\\$token", token);

                } else {
                  recoverUrlCp = recoverUrlCp + token;
                }

                Map<String, Object> ctx = new HashMap<String, Object>();

                ctx.put("recoverUrl", recoverUrlCp);
                ctx.put("userName", userName);

                Locale locale = null;

                String localeStr = a.asHuman().getLocale();

                if (localeStr == null) {
                  locale = Locale.getDefault();
                } else {
                  locale = ConversionUtils.toLocale(localeStr);
                }

                Template t = decTemplates.getTemplateFromName(RECOVER_PASSWORD_TEMPLATE);
                ResourceBundle b = decTemplates.getBundleFromName(RECOVER_PASSWORD_BUNDLE, locale);

                String subject =
                    MessageFormat.format(b.getString("restoreEmailSubject"), userName);

                String body =
                    decTemplates.getTemplateMessage(t, RECOVER_PASSWORD_BUNDLE, ctx, locale);

                emailSender.send(new InternetAddress(emailAddress), subject, body);

              }
            }

            response.setStatus(HttpServletResponse.SC_OK);

          } catch (MessagingException mex) {

            LOG.severe("Unexpected messaging exception while sending email:" + mex.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

          } catch (PersistenceException e) {

            LOG.severe("Unexpected persistence exception while sending email:" + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

          }

          break;
      }
    }
  }
}

package org.swellrt.beta.client.rest.operations;

import org.swellrt.beta.client.ServiceContext;
import org.swellrt.beta.client.rest.ServerOperation;
import org.swellrt.beta.client.rest.ServiceOperation;
import org.swellrt.beta.common.SException;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

public final class GetUserBatchOperation
    extends ServerOperation<GetUserBatchOperation.Options, GetUserBatchOperation.Response> {


  @JsType(isNative = true)
  public interface Options extends ServerOperation.Options {

    @JsProperty
    public String[] getId();


  }

  @JsType(isNative = true)
  public interface Response extends ServerOperation.Response {

  }

  public GetUserBatchOperation(ServiceContext context, Options options,
      ServiceOperation.Callback<Response> callback) {
    super(context, options, callback, new Response() {

    }.getClass());
  }

  @Override
  public ServerOperation.Method getMethod() {
    return ServerOperation.Method.GET;
  }



  @Override
  protected void buildRestParams() throws SException {

    if (options == null || options.getId() == null) {
      throw new SException(SException.MISSING_PARAMETERS);
    }

    String userIdQuery = "";
    if (options.getId() != null) {
      for (int i = 0; i < options.getId().length; i++) {
        if (!userIdQuery.isEmpty())
          userIdQuery += ";";
        userIdQuery += options.getId()[i];
      }
    }
    addQueryParam("p", userIdQuery);

    addPathElement("account");
  }



}

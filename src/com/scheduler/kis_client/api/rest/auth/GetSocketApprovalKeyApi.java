package com.scheduler.kis_client.api.rest.auth;

import com.scheduler.kis_client.api.Api;
import com.scheduler.kis_client.api.annotation.Body;
import com.scheduler.kis_client.api.annotation.Header;
import com.scheduler.kis_client.api.annotation.RestApi;
import com.scheduler.kis_client.api.annotation.auth.AppKeyRequired;
import com.scheduler.kis_client.api.annotation.auth.AppSecretRequired;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@RestApi(method = RestApi.Method.POST, path = "/oauth2/Approval")
@AppKeyRequired(location = AppKeyRequired.Location.BODY)
@AppSecretRequired(location = AppSecretRequired.Location.BODY, key="secretkey")
public class GetSocketApprovalKeyApi implements Api<GetSocketApprovalKeyResult> {

    @Header("content-type")
    private String contentType = "application/json; utf-8";

    @Body("grant_type")
    private String grantType = "client_credentials";

}
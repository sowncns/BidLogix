package com.qs.Backend.modules.system.manualhub.controller;

import com.qs.Backend.modules.system.manualhub.dto.OnlyOfficeCallbackRequest;
import com.qs.Backend.modules.system.manualhub.dto.OnlyOfficeConfigResponse;
import com.qs.Backend.modules.system.manualhub.service.OnlyOfficeService;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OnlyOfficeController {

    private final OnlyOfficeService onlyOfficeService;

    // Authenticated: called by erp-fe (with the user's JWT) to get the config
    // object handed straight to `new DocsAPI.DocEditor(...)`.
    @GetMapping("/manualhub/documents/{id}/onlyoffice-config")
    public ApiResponse<OnlyOfficeConfigResponse> config(@PathVariable Long id,
                                                         @RequestParam(required = false, defaultValue = "false") boolean readOnly) {
        return ApiResponse.ok(onlyOfficeService.buildConfig(id, readOnly), null);
    }

    // Public: called by the Document Server container itself, which carries
    // no user JWT. Response is the raw OnlyOffice callback contract
    // ({"error":0}), NOT wrapped in ApiResponse.
    @PostMapping("/public/manualhub/documents/{id}/onlyoffice-callback")
    public Map<String, Object> callback(@PathVariable Long id, @RequestBody OnlyOfficeCallbackRequest body) {
        return onlyOfficeService.handleCallback(id, body);
    }

    @PostMapping("/manualhub/documents/{id}/onlyoffice-forcesave")
    public ApiResponse<Map<String, String>> forceSave(@PathVariable Long id) {
        return ApiResponse.ok(onlyOfficeService.forceSave(id), null);
    }
}

package com.qs.Backend.modules.crm.campaign.controller;

import com.qs.Backend.modules.crm.campaign.dto.CampaignCreateRequest;
import com.qs.Backend.modules.crm.campaign.dto.CampaignListResponse;
import com.qs.Backend.modules.crm.campaign.dto.CampaignResponse;
import com.qs.Backend.modules.crm.campaign.dto.CampaignUpdateRequest;
import com.qs.Backend.modules.crm.campaign.dto.PublicCampaignResponse;
import com.qs.Backend.modules.crm.campaign.dto.PublicCampaignRegisterRequest;
import com.qs.Backend.modules.crm.campaign.dto.PublicCampaignRegisterResponse;
import com.qs.Backend.modules.crm.campaign.service.CampaignService;
import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService campaignService;

    @GetMapping("/campaigns")
    public ApiResponse<CampaignListResponse> list(@RequestParam(required = false) String status,
                                                   @RequestParam(name = "product_id", required = false) String productId,
                                                   @RequestParam(defaultValue = "50") int limit,
                                                   @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.ok(campaignService.list(status, productId, limit, offset), null);
    }

    @GetMapping("/campaigns/{id}")
    public ApiResponse<CampaignResponse> get(@PathVariable String id) {
        return ApiResponse.ok(campaignService.get(id), null);
    }

    @PostMapping("/campaigns")
    public ApiResponse<CampaignResponse> create(@Valid @RequestBody CampaignCreateRequest request,
                                                 @AuthenticationPrincipal AccountPrincipal principal) {
        String createdBy = principal == null ? null : String.valueOf(principal.getId());
        return ApiResponse.created(campaignService.create(request, createdBy), "Campaign created");
    }

    @PatchMapping("/campaigns/{id}")
    public ApiResponse<CampaignResponse> update(@PathVariable String id,
                                                 @RequestBody CampaignUpdateRequest request) {
        return ApiResponse.ok(campaignService.update(id, request), "Campaign updated");
    }

    @PostMapping("/campaigns/{id}/end")
    public ApiResponse<CampaignResponse> end(@PathVariable String id) {
        return ApiResponse.ok(campaignService.end(id), "Campaign ended");
    }

    @GetMapping("/public/campaigns/{slug}")
    public ApiResponse<PublicCampaignResponse> getPublic(@PathVariable String slug) {
        return ApiResponse.ok(campaignService.getPublic(slug), null);
    }

    @PostMapping("/public/campaigns/{slug}/register")
    public ApiResponse<PublicCampaignRegisterResponse> registerPublic(
            @PathVariable String slug,
            @RequestBody PublicCampaignRegisterRequest request
    ) {
        return ApiResponse.created(campaignService.registerPublic(slug, request), "Campaign registration created");
    }
}

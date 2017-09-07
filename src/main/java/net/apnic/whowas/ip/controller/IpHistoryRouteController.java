package net.apnic.whowas.ip.controller;

import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.error.MalformedRequestException;
import net.apnic.whowas.rdap.controller.RDAPControllerUtil;
import net.apnic.whowas.rdap.TopLevelObject;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Parsing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Rest controller for the RDAP /history/ip path segment.
 *
 * Controller is responsible for dealing with history state RDAP path segments.
 */
@RestController
@RequestMapping("/history/ip")
public class IpHistoryRouteController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(IpHistoryRouteController.class);

    private final RDAPControllerUtil rdapControllerUtil;

    @Autowired
    public IpHistoryRouteController(RDAPControllerUtil rdapControllerUtil)
    {
        this.rdapControllerUtil = rdapControllerUtil;
    }

    /**
     *
     */
    @RequestMapping(value="/**", method=RequestMethod.GET)
    public ResponseEntity<TopLevelObject> ipPathGet(
        HttpServletRequest request)
    {
        String param = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        LOGGER.debug("ip history GET path query for {}", param);

        IpInterval range = null;
        try
        {
            range = Parsing.parseCIDRInterval(param.substring(12));
        }
        catch(Exception ex)
        {
            throw new MalformedRequestException(ex);
        }

        return rdapControllerUtil.historyResponse(request, range);
    }
}


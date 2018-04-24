package net.apnic.rdapd.ip.controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import net.apnic.rdapd.error.MalformedRequestException;
import net.apnic.rdapd.history.ObjectHistory;
import net.apnic.rdapd.intervaltree.IntervalTree;
import net.apnic.rdapd.rdap.controller.RDAPControllerUtil;
import net.apnic.rdapd.rdap.controller.RDAPResponseMaker;
import net.apnic.rdapd.rdap.TopLevelObject;
import net.apnic.rdapd.types.IP;
import net.apnic.rdapd.types.IpInterval;
import net.apnic.rdapd.types.Parsing;
import net.apnic.rdapd.types.Tuple;

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

    private final IntervalTree<IP, ObjectHistory, IpInterval> historyTree;
    private final RDAPControllerUtil rdapControllerUtil;

    @Autowired
    public IpHistoryRouteController(
        IntervalTree<IP, ObjectHistory, IpInterval> historyTree,
        RDAPResponseMaker rdapResponseMaker)
    {
        this.historyTree = historyTree;
        this.rdapControllerUtil = new RDAPControllerUtil(rdapResponseMaker);
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

        int pfxCap = range.prefixSize() +
            (range.low().getAddressFamily() == IP.AddressFamily.IPv4 ? 8 : 16);

        List<ObjectHistory> ipHistory =
            historyTree
                .intersecting(range)
                .filter(t -> t.first().prefixSize() <= pfxCap)
                .sorted(Comparator.comparing(Tuple::first))
                .map(Tuple::second)
                .collect(Collectors.toList());

        return rdapControllerUtil.historyResponse(request, ipHistory);
    }
}


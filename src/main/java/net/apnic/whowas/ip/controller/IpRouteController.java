package net.apnic.whowas.ip.controller;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;

import net.apnic.whowas.error.MalformedRequestException;
import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.history.Revision;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.rdap.controller.RDAPControllerUtil;
import net.apnic.whowas.rdap.controller.RDAPResponseMaker;
import net.apnic.whowas.rdap.TopLevelObject;
import net.apnic.whowas.types.IP;
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

@RestController
@RequestMapping("/ip")
public class IpRouteController
{
    private final static Logger LOGGER = LoggerFactory.getLogger(IpRouteController.class);

    private final IntervalTree<IP, ObjectHistory, IpInterval> historyTree;
    private final RDAPControllerUtil rdapControllerUtil;

    @Autowired
    public IpRouteController(
        IntervalTree<IP, ObjectHistory, IpInterval> historyTree,
        RDAPResponseMaker rdapResponseMaker)
    {
        this.historyTree = historyTree;
        this.rdapControllerUtil = new RDAPControllerUtil(rdapResponseMaker);
    }

    @RequestMapping(value="/**", method=RequestMethod.GET)
    public ResponseEntity<TopLevelObject> ipPathGet(HttpServletRequest request)
    {
        String param = (String)request.getAttribute(
            HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        LOGGER.debug("ip GET path query for {}", param);

        IpInterval range = null;

        try
        {
            range = Parsing.parseCIDRInterval(param.substring(4));
        }
        catch(Exception ex)
        {
            throw new MalformedRequestException(ex);
        }

        return rdapControllerUtil.singleObjectResponse(request,
            mostCurrent(range).map(Revision::getContents).orElse(null));
    }

    @RequestMapping(value="/**", method=RequestMethod.HEAD)
    public ResponseEntity<TopLevelObject> ipPathHead(HttpServletRequest request)
    {
        String param = (String)request.getAttribute(
            HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        LOGGER.debug("ip HEAD path query for {}", param);

        IpInterval range = null;

        try
        {
            range = Parsing.parseCIDRInterval(param.substring(4));
        }
        catch(Exception ex)
        {
            throw new MalformedRequestException(ex);
        }

        return rdapControllerUtil.singleObjectResponse(request,
            mostCurrent(range).map(Revision::getContents).orElse(null));
    }

    private Optional<Revision> mostCurrent(IpInterval range)
    {
        return historyTree.equalToAndLeastSpecific(range)
            .filter(t -> t.second().mostCurrent().isPresent())
            .reduce((a, b) -> a.first().compareTo(b.first()) >= 0 ? b : a)
            .flatMap(t -> t.second().mostCurrent());
    }
}

package net.floodlightcontroller.mactracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.Ethernet;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MACTracker implements IOFMessageListener, IFloodlightModule {

	/*Now that we have our skeleton class, we have to implement the correct functions to make the module loadable. Lets start by registering some member variables we’ll need into the class. Since we are listening to OpenFlow messages we need to register with the FloodlightProvider (IFloodlightProviderService class). We also need a set to store macAddresses we’ve seen. Finally, we need a logger to output what we’ve seen.
	 */

	protected IFloodlightProviderService floodlightProvider;
	protected Set<Long> macAddresses;
	protected static Logger logger;

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	/*Now we need to wire it up to the module loading system. We tell the module loader we depend on it by modifying the getModuleDependencies() function.
	*/
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
	    Collection<Class<? extends IFloodlightService>> l =
	        new ArrayList<Class<? extends IFloodlightService>>();
	    l.add(IFloodlightProviderService.class);
	    return l;
	}

	/*Now its time to create our Init method. Init is called early in the controller startup process — it primarily is run to load dependencies and initialize datastructures.
	*/

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
	    floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
	    macAddresses = new ConcurrentSkipListSet<Long>();
	    logger = LoggerFactory.getLogger(MACTracker.class);
	}

	/*Now it’s time to implement the basic listener. We’ll register for PACKET_IN messages in our startUp method. Here we are assured other modules we depend on are already initialized.
	*/
	@Override
	public void startUp(FloodlightModuleContext context) {
	    floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
	}

	/*We also have to put in an ID for our OFMessage listener. This is done in the getName() call.
	*/
	@Override
	public String getName() {
	    return MACTracker.class.getSimpleName();
	}

	/*Now we have to define the behavior we want for PACKET_IN messages. Note that we return Command.CONTINUE to allow this message to continue to be handled by other PACKET_IN handlers as well.
	*/
	@Override
	   public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
	        Ethernet eth =
	                IFloodlightProviderService.bcStore.get(cntx,
	                                            IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

	        Long sourceMACHash = (long) eth.getSourceMACAddress().length;
	        if (!macAddresses.contains(sourceMACHash)) {
	            macAddresses.add(sourceMACHash);
	            logger.info("MAC Address: {} seen on switch: {}",
	                    eth.getSourceMACAddress().toString(),
	                    sw.getId());
	        }
	        return Command.CONTINUE;
	    }

}

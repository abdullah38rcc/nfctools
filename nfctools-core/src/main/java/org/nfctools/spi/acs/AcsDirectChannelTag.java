package org.nfctools.spi.acs;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.nfctools.NfcException;
import org.nfctools.api.ApduTag;
import org.nfctools.api.Tag;
import org.nfctools.api.TagType;
import org.nfctools.scio.Command;
import org.nfctools.scio.Response;
import org.nfctools.utils.NfcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcsDirectChannelTag extends Tag implements ApduTag {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Card card;

	public AcsDirectChannelTag(TagType tagType, byte[] generalBytes, Card card) {
		super(tagType, generalBytes);
		this.card = card;
	}

	@Override
	public Response transmit(Command command) {
		try {
			CommandAPDU commandAPDU = null;
			if (command.isDataOnly()) {
				commandAPDU = new CommandAPDU(0xff, 0, 0, 0, command.getData(), command.getOffset(),
						command.getLength());
			}
			else
				throw new NfcException("Only data mode supported");

			if (log.isDebugEnabled())
				log.debug("command: " + NfcUtils.convertBinToASCII(commandAPDU.getBytes()));

			byte[] transmitControlResponse = card.transmitControlCommand(Acs.IOCTL_SMARTCARD_ACR122_ESCAPE_COMMAND,
					commandAPDU.getBytes());
			ResponseAPDU responseAPDU = new ResponseAPDU(transmitControlResponse);
			if (log.isDebugEnabled())
				log.debug("response: " + NfcUtils.convertBinToASCII(responseAPDU.getBytes()));

			return new Response(responseAPDU.getSW1(), responseAPDU.getSW2(), responseAPDU.getData());
		}
		catch (CardException e) {
			throw new NfcException(e);
		}
	}

}
package org.tron.core.capsule;

import com.google.protobuf.ByteString;
import org.tron.protos.contract.Common;

public class BytesCapsule implements ProtoCapsule<Common.ByteArray> {

  private byte[] bytes;

  public BytesCapsule(byte[] bytes) {
    this.bytes = bytes;
  }

  @Override
  public byte[] getData() {
    return bytes;
  }

  @Override
  public Common.ByteArray getInstance() {
    return Common.ByteArray.newBuilder().setData(ByteString.copyFrom(bytes)).build();
  }
}

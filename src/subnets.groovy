import java.awt.geom.Arc2D.Double;
import com.google.common.net.InetAddresses

//println InetAddresses.fromInteger( (int)-1 << ( 32 - 3 ) )

def testSubnets = [
//  '192.168.51.71':'255.255.128.0',
//  '192.168.51.72':'255.255.192.0',
  '192.168.51.73':'255.255.255.0',
];
def testAddrs = [ '8.8.8.8', '192.168.52.128', '192.168.51.1' ]
testSubnets.each{ addr, subnet ->
  int subnetInt = InetAddresses.coerceToInteger(InetAddress.getByName(subnet));
  int addrInt = InetAddresses.coerceToInteger(InetAddress.getByName(addr));
  int networkId = addrInt&subnetInt;
  testAddrs.collect{ InetAddress.getByName(it) }.each{ testAddr ->
    int testAddrInt = InetAddresses.coerceToInteger(testAddr);
    println """\
${Integer.toBinaryString(addrInt)}  Address ${addr} 
${Integer.toBinaryString(subnetInt)}  Subnet  ${subnet} 
${Integer.toBinaryString(addrInt&subnetInt)}  Network ID  ${InetAddresses.fromInteger( networkId ).getHostAddress( )}/${Integer.lowestOneBit( subnetInt ).toString( ).size( )}
${Integer.toBinaryString(testAddrInt)}  Test Address ${testAddr}
${Integer.toBinaryString(testAddrInt&subnetInt)}  testAddr masked
${testAddr} ${(testAddrInt&subnetInt)==(addrInt&subnetInt)?"is in":"is not in"} subnet ${addr}/${subnet}"""
  }
}
println Math.round(Math.log(Integer.lowestOneBit(11111111111111111111111100000000))/Math.log(2)) 

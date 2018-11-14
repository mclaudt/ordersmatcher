package matcher

case class ClientState(name: Client, money: Int, stock: Map[Char, Quantity] = Map.empty.withDefaultValue(0))

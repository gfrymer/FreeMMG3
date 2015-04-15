/// SUPER classe de todos os tipos de eventos (por enquanto soh existe um, o AvatarEvent,
/// que eh o mesmo evento mandado pelo jogador pro seu membro responsavel na celula pra requisitar
/// a movimentacao quanto o evento mandado pelo membro pro jogador autorizando a movimentacao

public abstract class Event
{
	public abstract int getSize();
}
